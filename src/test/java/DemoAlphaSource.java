import bdv.spimdata.SpimDataMinimal;
import bdv.util.*;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

public class DemoAlphaSource {
    static BdvHandle bdvh;

    public static void main(final String... args) {
        BdvOptions options = BdvOptions.options();
        //options = options.accumulateProjectorFactory(new DefaultProjectorFactory());
        options = options.accumulateProjectorFactory(new BasicAlphaProjectorFactory());

        MaskConverter mc = new MaskConverter();

        AbstractSpimData sd = BdvSampleDatasets.getTestSpimData();

        List<BdvStackSource<?>> stackSources = BdvFunctions.show(sd, options);
        stackSources.get(0).setDisplayRange(0,255);

        bdvh = stackSources.get(0).getBdvHandle();

        Source<FloatType> alpha = new SourceAlpha(stackSources.get(0).getSources().get(0).getSpimSource(), 1f);

        SourceAndConverter<FloatType> alpha_sac =
                new SourceAndConverter<>(alpha, mc);

        bdvh.getViewerPanel().state().addSource(alpha_sac); // No converter setup
        bdvh.getViewerPanel().state().setSourceActive(alpha_sac, true);

        sd = BdvSampleDatasets.getTestSpimData();

        BdvSampleDatasets.shiftSpimData((SpimDataMinimal) sd,20,0);

        stackSources = BdvFunctions.show(sd, options.addTo(bdvh));
        stackSources.get(0).setDisplayRange(0,255);

        SourceAlpha alpha_anim = new SourceAlpha(stackSources.get(0).getSources().get(0).getSpimSource(), 0.5f);

        alpha_sac = new SourceAndConverter<>(alpha_anim, mc);

        bdvh.getViewerPanel().state().addSource(alpha_sac); // No converter setup
        bdvh.getViewerPanel().state().setSourceActive(alpha_sac, true);

    }
}
