
import bdv.util.*;
import bdv.viewer.render.AccumulateProjectorFactory;
import net.imglib2.type.numeric.ARGBType;

import java.util.function.Function;

public class PerfTests {

    public static boolean testPerf = true;

    public static boolean closeAfterTest = true;

    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception
     */
    public static void main(final String... args) {

        test(new DefaultProjectorFactory(), BdvSampleDatasets::twoImages);
        test(new SlowProjectorFactory(), BdvSampleDatasets::twoImages);
        test(new TakeFirstProjectorFactory(), BdvSampleDatasets::twoImages);

        test(new DefaultProjectorFactory(), BdvSampleDatasets::twentyFiveImages);
        test(new SlowProjectorFactory(), BdvSampleDatasets::twentyFiveImages);
        test(new TakeFirstProjectorFactory(), BdvSampleDatasets::twentyFiveImages);

    }

    static public void test(AccumulateProjectorFactory<ARGBType> accumulator, Function<BdvOptions, BdvHandle> dataprovider) {
        BdvOptions options = BdvOptions.options().accumulateProjectorFactory(accumulator);
        BdvHandle bdvh = dataprovider.apply(options);
        if (testPerf) System.out.print(BdvProbeFPS.getStdMsPerFrame(bdvh)+" ms per frame");
        System.out.println("");
        if (closeAfterTest) bdvh.close();
    }


}
