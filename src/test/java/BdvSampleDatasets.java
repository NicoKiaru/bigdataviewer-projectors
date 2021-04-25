import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.util.*;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.registration.ViewTransformAffine;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.List;

public class BdvSampleDatasets {

    public static BdvHandle twoImages(BdvOptions options) throws SpimDataException {
        // Display 2 sources shifted in space

        String fn = "src/test/resources/mri-stack.xml";

        SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( fn );

        List<BdvStackSource<?>> stackSources = BdvFunctions.show(spimData, options);
        stackSources.get(0).setDisplayRange(0,255);

        BdvHandle bdvh = stackSources.get(0).getBdvHandle(); // Get current bdv window

        spimData = new XmlIoSpimDataMinimal().load( fn );

        shiftSpimData(spimData, 50, 0);

        stackSources = BdvFunctions.show(spimData, BdvOptions.options().addTo(bdvh));
        stackSources.get(0).setDisplayRange(0,255);

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
