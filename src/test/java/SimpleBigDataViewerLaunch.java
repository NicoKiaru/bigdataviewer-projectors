
import bdv.util.*;
import net.imagej.ImageJ;

public class SimpleBigDataViewerLaunch {

    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception
     */
    public static void main(final String... args) throws Exception {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        BdvHandle bdvh;
        BdvOptions options;

        System.out.println("- Testing simple image and standard projector");
        options = BdvOptions.options();
        bdvh = BdvSampleDatasets.twoImages(options);
        System.out.println("Test frame rate : "+PerfTester.getStdMsPerFrame(bdvh)+" ms per frame");
        bdvh.close();

        System.out.println("- Testing simple image and terrible projector");
        options = BdvOptions.options().accumulateProjectorFactory(new SlowProjectorFactory());
        bdvh = BdvSampleDatasets.twoImages(options);
        System.out.println("Test frame rate : "+PerfTester.getStdMsPerFrame(bdvh)+" ms per frame");
        bdvh.close();

    }


}
