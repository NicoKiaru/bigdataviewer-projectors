package bdv.util;

import net.imglib2.converter.Converter;
import net.imglib2.display.ColorConverter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Converter for alpha Mask used in alpha converter
 */
public class MaskConverter implements ColorConverter, Converter<FloatType, ARGBType> {
    @Override
    public void convert(FloatType input, ARGBType output) {
        //output.set(ARGBType.rgba(255,255,255,120));//Float.floatToIntBits(input.get()));
        output.set(Float.floatToIntBits(input.get()));
    }

    @Override
    public ARGBType getColor() {
        return null;
    }

    @Override
    public void setColor(ARGBType c) {

    }

    @Override
    public boolean supportsColor() {
        return false;
    }

    @Override
    public double getMin() {
        return 0;
    }

    @Override
    public double getMax() {
        return 255;
    }

    @Override
    public void setMin(double min) {

    }

    @Override
    public void setMax(double max) {

    }
}
