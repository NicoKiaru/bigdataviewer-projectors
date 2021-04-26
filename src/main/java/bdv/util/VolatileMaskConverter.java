package bdv.util;

import net.imglib2.Volatile;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileFloatType;

/**
 * Converter for alpha Mask used in alpha converter
 */
public class VolatileMaskConverter implements Converter<VolatileFloatType, ARGBType> {
    @Override
    public void convert(VolatileFloatType input, ARGBType output) {
        //output.set(ARGBType.rgba(255,255,255,120));
        output.set(Float.floatToIntBits(input.get().get()));
    }

}
