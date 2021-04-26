package bdv.util;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.*;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

import java.util.function.BiConsumer;

/**
 * Alpha channel of a source
 * FloatType to fit in 32 bits
 *
 * Only works if the origin source is backed by a simple source with affine transformed RAI
 *
 * alpha = 1f : complete Opacity, 0 : fully transparent
 *
 * alpha = 0 outside the transformed RAI
 *
 */

public class SourceAlpha implements Source<FloatType> {

    protected final DefaultInterpolators< FloatType > interpolators = new DefaultInterpolators<>();

    final Source<?> origin;

    public SourceAlpha(Source<?> origin) {
        this(origin, 1f);
    }

    public SourceAlpha(Source<?> origin, float alpha) {
        this.origin = origin;
        this.alpha = alpha;
    }

    float alpha; // Complete Opacity

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @Override
    public boolean isPresent(int t) {
        return origin.isPresent(t);
    }

    @Override
    public RandomAccessibleInterval<FloatType> getSource(int t, int level) {
        final float finalAlpha = alpha;

        final RandomAccessible< FloatType > randomAccessible =
                new FunctionRandomAccessible<>( 3, () -> (loc, out) -> out.setReal( finalAlpha*loc.getIntPosition(0)/120.0 ), FloatType::new );

        return Views.interval(randomAccessible, origin.getSource(t, level));
    }

    @Override
    public RealRandomAccessible<FloatType> getInterpolatedSource(int t, int level, Interpolation method) {
        ExtendedRandomAccessibleInterval<FloatType, RandomAccessibleInterval< FloatType >>
                eView = Views.extendZero(getSource( t, level ));
        RealRandomAccessible< FloatType > realRandomAccessible = Views.interpolate( eView, interpolators.get(method) );
        return realRandomAccessible;
    }

    @Override
    public void getSourceTransform(int t, int level, AffineTransform3D transform) {
        origin.getSourceTransform(t,level,transform);
    }

    @Override
    public FloatType getType() {
        return new FloatType();
    }

    @Override
    public String getName() {
        return origin.getName()+"_alpha";
    }

    @Override
    public VoxelDimensions getVoxelDimensions() {
        return origin.getVoxelDimensions();
    }

    @Override
    public int getNumMipmapLevels() {
        return origin.getNumMipmapLevels();
    }
}
