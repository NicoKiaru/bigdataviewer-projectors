import bdv.util.*;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.type.numeric.real.FloatType;

import java.util.List;

/**
 * Cannot be tested yet, need to understand / solve {@link WeirdIssueAccumulatorIgnored} first ...
 */

public class DemoAlphaSource {

    public static void main(final String... args) {
        BdvOptions options = BdvOptions.options();
        options = options.accumulateProjectorFactory(new SlowProjectorFactory());

        AbstractSpimData sd = BdvSampleDatasets.getTestSpimData();

        List<BdvStackSource<?>> stackSources = BdvFunctions.show(sd, options);
        stackSources.get(0).setDisplayRange(0,255);

        BdvHandle bdvh = stackSources.get(0).getBdvHandle();

        //Source<FloatType> alpha = new SourceAlpha(stackSources.get(0).getSources().get(0).getSpimSource(), 0.5f);
        //SourceAndConverter<FloatType> alpha_sac = new SourceAndConverter<>(alpha, new MaskConverter());

        //bdvh.getViewerPanel().state().addSource(alpha_sac); // No converter setup

        //BdvFunctions.show(alpha);//, options.addTo(bdvh));

    }
}
