import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.util.*;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.registration.ViewTransformAffine;
import net.imglib2.realtransform.AffineTransform3D;
import org.python.modules._marshal;

import java.util.List;

public class BdvSampleDatasets {

    public static BdvHandle twoImages(BdvOptions options) {
        // Display 2 sources shifted in space
        System.out.print("2 Images\t");
        String fn = "src/test/resources/mri-stack.xml";

        SpimDataMinimal spimData = null;
        try {
            spimData = new XmlIoSpimDataMinimal().load( fn );
        } catch (SpimDataException e) {
            e.printStackTrace();
            return null;
        }

        List<BdvStackSource<?>> stackSources = BdvFunctions.show(spimData, options);
        stackSources.get(0).setDisplayRange(0,255);

        BdvHandle bdvh = stackSources.get(0).getBdvHandle(); // Get current bdv window

        try {
            spimData = new XmlIoSpimDataMinimal().load( fn );
        } catch (SpimDataException e) {
            e.printStackTrace();
            return null;
        }
        shiftSpimData(spimData, 50, 0);

        stackSources = BdvFunctions.show(spimData, BdvOptions.options().addTo(bdvh));
        stackSources.get(0).setDisplayRange(0,255);

        return bdvh;
    }

    public static BdvHandle twentyFiveImages(BdvOptions options) {
        // Display 2 sources shifted in space
        System.out.print("100 Images\t");
        String fn = "src/test/resources/mri-stack.xml";
        SpimDataMinimal spimData = null;
        try {
            spimData = new XmlIoSpimDataMinimal().load( fn );
        } catch (SpimDataException e) {
            e.printStackTrace();
            return null;
        }
        List<BdvStackSource<?>> stackSources = BdvFunctions.show(spimData, options);
        stackSources.get(0).setDisplayRange(0,255);

        BdvHandle bdvh = stackSources.get(0).getBdvHandle(); // Get current bdv window

        for (int x=0;x<5;x++) {
            for (int y=0;y<5;y++) {
                try {
                    spimData = new XmlIoSpimDataMinimal().load( fn );
                } catch (SpimDataException e) {
                    e.printStackTrace();
                    return null;
                }

                shiftSpimData(spimData, 50*x, 50*y);

                stackSources = BdvFunctions.show(spimData, BdvOptions.options().addTo(bdvh));
                stackSources.get(0).setDisplayRange(0,255);
            }
        }

        AffineTransform3D at3D = new AffineTransform3D();
        bdvh.getViewerPanel().state().getViewerTransform(at3D);
        at3D.scale(0.6);
        bdvh.getViewerPanel().state().setViewerTransform(at3D);

        return bdvh;
    }

    public static void shiftSpimData(SpimDataMinimal spimData, int x, int y) {
        AffineTransform3D transform = new AffineTransform3D();
        transform.translate(x,y,0);
        spimData.getViewRegistrations()
                .getViewRegistration(0,0)
                .concatenateTransform(new ViewTransformAffine("ShiftXY", transform));
    }
}
