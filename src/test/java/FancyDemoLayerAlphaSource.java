import bdv.spimdata.SpimDataMinimal;
import bdv.util.*;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FancyDemoLayerAlphaSource {

    static BdvHandle bdvh;

    static Map<SourceAndConverter, SourceAndConverter> sourceToAlpha = new ConcurrentHashMap<>();
    static Map<SourceAndConverter, LayerAlphaProjectorFactory.Layer> sourceToLayer = new ConcurrentHashMap<>();

    public static class DefaultLayer implements LayerAlphaProjectorFactory.Layer {

        float alpha;
        int id;
        boolean skip;

        public DefaultLayer(float alpha, int id, boolean skip) {
            this.alpha = alpha;
            this.id = id;
            this.skip = skip;
        }

        public float getAlpha(){
            return alpha;
        }

        public int getBlendingMode() {
            // 0 = SUM, 1 = AVERAGE TODO , currently only sum
            return 0;
        }

        public boolean skip() {
            return skip;
        }

        public int getId() {
            return id;
        }
    }

    public static void main(final String... args) {

        List<LayerAlphaProjectorFactory.Layer> layers = new ArrayList<>();
        layers.add(new DefaultLayer(1f, 0, true));
        layers.add(new DefaultLayer(0.4f, 1, false));
        layers.add(new DefaultLayer(0.8f, 2, false));

        BdvOptions options = BdvOptions.options();
        options = options.accumulateProjectorFactory(new LayerAlphaProjectorFactory(new LayerAlphaProjectorFactory.SourcesMetadata() {
            @Override
            public boolean isAlphaSource(SourceAndConverter sac) {
                if (sourceToAlpha.containsValue(sac)) return true;
                return false;
            }

            @Override
            public boolean hasAlphaSource(SourceAndConverter sac) {
                if (sourceToAlpha.containsKey(sac)) {
                    return sourceToAlpha.get(sac) != null;
                }
                return false;
            }

            @Override
            public SourceAndConverter getAlphaSource(SourceAndConverter sac) {
                if (sourceToAlpha.containsKey(sac)) {
                    return sourceToAlpha.get(sac);
                }
                return null;
            }
        },
            new LayerAlphaProjectorFactory.LayerMetadata() {
                @Override
                public LayerAlphaProjectorFactory.Layer getLayer(SourceAndConverter sac) {
                    if (sourceToLayer.containsKey(sac)) {
                        return sourceToLayer.get(sac);
                    } else {
                        return layers.get(0);
                    }
                }
            }));

        AbstractSpimData sd = BdvSampleDatasets.getTestSpimData();

        List<BdvStackSource<?>> stackSources = BdvFunctions.show(sd, options);
        stackSources.get(0).setDisplayRange(0,255);

        bdvh = stackSources.get(0).getBdvHandle();

        Source<FloatType> alpha = new SourceAlpha(stackSources.get(0).getSources().get(0).getSpimSource(), 1f);
        SourceAndConverter<FloatType> alpha_sac =
                new SourceAndConverter<>(alpha, new MaskConverter());

        bdvh.getViewerPanel().state().addSource(alpha_sac); // No converter setup
        bdvh.getViewerPanel().state().setSourceActive(alpha_sac, true);
        sourceToAlpha.put(bdvh.getViewerPanel().state().getSources().get(0), alpha_sac);
        sourceToLayer.put(bdvh.getViewerPanel().state().getSources().get(0), layers.get(1));

        for (int x=0;x<5;x++) {
            for (int y=0;y<5;y++) {
                appendTestSpimdata(bdvh, 200*x, 200*y, layers.get( (x+y) % 2 + 1));
            }
        }

    }

    private static void appendTestSpimdata(BdvHandle bdvh, float x, float y, LayerAlphaProjectorFactory.Layer layer) {
        SpimDataMinimal sd = BdvSampleDatasets.getTestSpimData();

        BdvSampleDatasets.shiftSpimData( sd,(int)x,(int)y);

        List<BdvStackSource<?>> stackSources = BdvFunctions.show(sd, BdvOptions.options().addTo(bdvh));
        stackSources.get(0).setDisplayRange(0,255);

        int nSources = bdvh.getViewerPanel().state().getSources().size();

        SourceAlpha alpha = new SourceAlpha(bdvh.getViewerPanel().state().getSources().get(nSources-1).getSpimSource(), 1f);

        SourceAndConverter<FloatType> alpha_sac = new SourceAndConverter<>(alpha, new MaskConverter());

        bdvh.getViewerPanel().state().addSource(alpha_sac); // No converter setup
        bdvh.getViewerPanel().state().setSourceActive(alpha_sac, true);

        sourceToAlpha.put(bdvh.getViewerPanel().state().getSources().get(nSources-1), alpha_sac);
        sourceToLayer.put(bdvh.getViewerPanel().state().getSources().get(nSources-1), layer);

    }
}
