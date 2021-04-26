package bdv.util;

import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Converter for alpha Mask used in alpha converter
 */
public class MaskConverter implements Converter<FloatType, ARGBType> {
    @Override
    public void convert(FloatType input, ARGBType output) {
        //output.set(ARGBType.rgba(255,255,255,120));
        output.set(Float.floatToIntBits(input.get()));
    }

}
