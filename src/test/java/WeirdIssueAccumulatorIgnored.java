import bdv.util.*;
import bdv.viewer.render.AccumulateProjectorFactory;
import net.imglib2.type.numeric.ARGBType;

import java.util.function.Function;

public class WeirdIssueAccumulatorIgnored {

    public static void main(final String... args) {
        test(new BlackProjectorFactory(), BdvSampleDatasets::oneImage); // We got an image!!!
        test(new BlackProjectorFactory(), BdvSampleDatasets::twoImages); // No image, as expected
    }

    static public void test(AccumulateProjectorFactory<ARGBType> accumulator, Function<BdvOptions, BdvHandle> dataprovider) {
        BdvOptions options = BdvOptions.options().accumulateProjectorFactory(accumulator);
        BdvHandle bdvh = dataprovider.apply(options);
    }
}
