package bdv.util;

import bdv.viewer.SourceAndConverter;
import bdv.viewer.render.AccumulateProjector;
import bdv.viewer.render.AccumulateProjectorFactory;
import bdv.viewer.render.VolatileProjector;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class LayerAlphaProjectorFactory implements AccumulateProjectorFactory<ARGBType> {

    final SourcesMetadata sourcesMeta;
    final LayerMetadata layerMeta;

    public LayerAlphaProjectorFactory(SourcesMetadata sourcesMeta, LayerMetadata layerMeta) {
        System.out.print(this.getClass()+"\t");
        this.sourcesMeta = sourcesMeta;
        this.layerMeta = layerMeta;
    }

    public VolatileProjector createProjector(
            final List< VolatileProjector > sourceProjectors,
            final List<SourceAndConverter< ? >> sources,
            final List<? extends RandomAccessible<? extends ARGBType>> sourceScreenImages,
            final RandomAccessibleInterval<ARGBType> targetScreenImage,
            final int numThreads,
            final ExecutorService executorService )
    {
        return new AccumulateProjectorARGBGeneric(sourcesMeta, layerMeta, sourceProjectors, sources, sourceScreenImages, targetScreenImage, numThreads, executorService );
    }

    public static class AccumulateProjectorARGBGeneric extends AccumulateProjector< ARGBType, ARGBType >
    {

        final boolean[] source_is_alpha; // flags if the source in an alpha source ( not displayed )
        final boolean[] source_has_alpha; // flag if the source has an alpha channel, present in the list of sources
        final int[] source_linked_alpha_source_index; // index of the alpha channel, if any ( has_alpha is false otherwise )
        final int[] sources_sorted_indices; // index of sources, ordered from lower to higher layer
        final boolean[] source_layer_skip;
        final float[] source_layer_alpha;
        final int[] source_layer_mode;
        final boolean[] source_layer_next; // Flags when it's the start of the next layer

        public AccumulateProjectorARGBGeneric(
                SourcesMetadata sourcesMeta,
                LayerMetadata layerMeta,
                final List< VolatileProjector > sourceProjectors,
                final List<SourceAndConverter< ? >> sources,
                final List< ? extends RandomAccessible< ? extends ARGBType > > sourceScreenImages,
                final RandomAccessibleInterval< ARGBType > target,
                final int numThreads,
                final ExecutorService executorService
                )
        {
            super( sourceProjectors, sourceScreenImages, target, numThreads, executorService );
            source_linked_alpha_source_index = new int[sources.size()];
            source_is_alpha = new boolean[sources.size()];
            source_has_alpha = new boolean[sources.size()];

            // Let's sort which sources are alpha, which are not, and which contain alpha channels, actually present in the projector
            for (int index_source=0; index_source<sources.size(); index_source++) {
                SourceAndConverter source = sources.get(index_source);
                int index_alpha_source;
                if (sourcesMeta.isAlphaSource(source)) {
                    source_is_alpha[index_source] = true;
                } else {
                    // It's not an alpha source
                    if (sourcesMeta.hasAlphaSource(source)) {
                        // It has an alpha, source, but is it in the list of sources ?
                        index_alpha_source = sources.indexOf(sourcesMeta.getAlphaSource(source)); // returns -1 if the source does not exist
                        if (index_alpha_source!=-1) {
                            source_has_alpha[index_source] = true;
                            source_linked_alpha_source_index[index_source] = index_alpha_source;
                        } else {
                            source_has_alpha[index_source] = false;
                        }
                    } else {
                        source_has_alpha[index_source] = false;
                    }
                }
            }

            // Now let's take layers into account
            // Simply puts all layers into an array
            Layer[] layer_array = new Layer[sources.size()];
            for (int index_source=0; index_source<sources.size(); index_source++) {
                layer_array[index_source] = layerMeta.getLayer(sources.get(index_source));
            }

            // We need to re-index the sources to iterate them from lowest layer to the highest id of the layers
            sources_sorted_indices = IntStream.range(0,sources.size())
                    .boxed()
                    .sorted(Comparator.comparing(index -> layerMeta.getLayer(sources.get(index)), Layer::compareTo))
                    .mapToInt(i -> i).toArray();

            // Many duplicated values, but convenient : stores layer properties for all sources
            source_layer_skip = new boolean[sources.size()];
            source_layer_alpha = new float[sources.size()];
            source_layer_mode = new int[sources.size()];
            source_layer_next= new boolean[sources.size()]; // Flags when it's the start of the next layer

            for (int i=0;i<sources.size();i++) {
                int source_index = sources_sorted_indices[i];
                Layer current_layer = layer_array[source_index];
                System.out.println("-- "+i+":"+source_index);
                System.out.println("Source name = "+sources.get(source_index).getSpimSource().getName());
                System.out.println("Current Layer Id = "+current_layer.getId());
                System.out.println("Should be equal to "+layerMeta.getLayer(sources.get(source_index)).getId());
                source_layer_skip[source_index] = current_layer.skip();
                source_layer_alpha[source_index] = current_layer.getAlpha();
                source_layer_mode[source_index] = current_layer.getBlendingMode();
                if (i==sources.size()-1) {
                    System.out.println("Last : true");
                    source_layer_next[source_index] = true;
                } else {
                    System.out.println("i="+i+" source_index="+source_index+" next source index = "+sources_sorted_indices[i+1]);
                    System.out.println("current layer id = "+current_layer.getId());
                    System.out.println("next layer id = "+layer_array[sources_sorted_indices[i+1]].getId());
                    source_layer_next[source_index] = ((current_layer.getId())!=(layer_array[sources_sorted_indices[i+1]].getId()));
                }

                System.out.println("source_layer_alpha["+source_index+"] = "+source_layer_alpha[source_index]);
                System.out.println("source_layer_mode["+source_index+"] = "+source_layer_mode[source_index]);
                System.out.println("source_layer_next["+source_index+"] = "+source_layer_next[source_index]);
                System.out.println("source_layer_skip["+source_index+"] = "+source_layer_skip[source_index]);
            }

            System.out.println("---------------");
            for (int iSource = 0;iSource<sources.size();iSource++) {
                System.out.println("is_alpha["+iSource+"] = "+ source_is_alpha[iSource]);
                System.out.println("has_alpha["+iSource+"] = "+ source_has_alpha[iSource]);
                System.out.println("sources_alpha_index["+iSource+"] = "+ source_linked_alpha_source_index[iSource]);
                //System.out.println("sources_sorted_indices["+iSource+"] = "+sources_sorted_indices[iSource]);
                System.out.println("source_layer_alpha["+iSource+"] = "+source_layer_alpha[iSource]);
                System.out.println("source_layer_mode["+iSource+"] = "+source_layer_mode[iSource]);
                System.out.println("source_layer_next["+iSource+"] = "+source_layer_next[iSource]);
                System.out.println("source_layer_skip["+iSource+"] = "+source_layer_skip[iSource]);
                System.out.println("--");
            }
            System.out.println("---------------");

        }

        @Override
        protected void accumulate(final Cursor< ? extends ARGBType >[] accesses, final ARGBType target )
        {
            int aSum = 0, rSum = 0, gSum = 0, bSum = 0;
            int aLayer = 0, rLayer = 0, gLayer = 0, bLayer = 0;

            // Initialisation before the loop
            int current_source_index = sources_sorted_indices[0];
            boolean skip_current_layer = source_layer_skip[current_source_index];

            for (int i=0;i<accesses.length;i++) {
                current_source_index = sources_sorted_indices[i];
                skip_current_layer = source_layer_skip[current_source_index];
                if (!skip_current_layer) {
                    if (!source_is_alpha[current_source_index]) {
                        // Has an alpha channel : uses the alpha channel for the projection
                        if (source_has_alpha[current_source_index]) {
                            final Cursor< ? extends ARGBType > access = accesses[current_source_index];
                            final Cursor< ? extends ARGBType > access_alpha = accesses[source_linked_alpha_source_index[current_source_index]];
                            final float alpha = Float.intBitsToFloat(access_alpha.get().get());
                            final int value = access.get().get();
                            final int a = (int) (ARGBType.alpha( value )*alpha);
                            final int r = (int) (ARGBType.red( value )*alpha);
                            final int g = (int) (ARGBType.green( value )*alpha);
                            final int b = (int) (ARGBType.blue( value )*alpha);
                            aLayer += a;
                            rLayer += r;
                            gLayer += g;
                            bLayer += b;
                        } else {
                            // No alpha channel: standard sum
                            final int value = accesses[current_source_index].get().get();
                            final int a = ARGBType.alpha( value );
                            final int r = ARGBType.red( value );
                            final int g = ARGBType.green( value );
                            final int b = ARGBType.blue( value );

                            aLayer += a;
                            rLayer += r;
                            gLayer += g;
                            bLayer += b;
                        }
                    }
                }
                if (source_layer_next[current_source_index]) {
                    // Append layer value
                    if (!skip_current_layer) {
                        float alpha = source_layer_alpha[current_source_index];
                        aSum = (int)((1-alpha)*aSum+alpha*aLayer);
                        rSum = (int)((1-alpha)*rSum+alpha*rLayer);
                        gSum = (int)((1-alpha)*gSum+alpha*gLayer);
                        bSum = (int)((1-alpha)*bSum+alpha*bLayer);
                    }
                    aLayer = 0;
                    rLayer = 0;
                    gLayer = 0;
                    bLayer = 0;
                }
            }

            if ( aSum > 255 )
                aSum = 255;
            if ( rSum > 255 )
                rSum = 255;
            if ( gSum > 255 )
                gSum = 255;
            if ( bSum > 255 )
                bSum = 255;
            target.set( ARGBType.rgba( rSum, gSum, bSum, aSum ) );
        }
    }

    public interface SourcesMetadata {
        boolean isAlphaSource(SourceAndConverter sac);
        boolean hasAlphaSource(SourceAndConverter sac);
        SourceAndConverter getAlphaSource(SourceAndConverter sac);
    }

    public interface LayerMetadata {
        Layer getLayer(SourceAndConverter sac);
    }

    public interface Layer extends Comparable<Layer> {
        float getAlpha();
        int getBlendingMode(); // 0 = SUM, 1 = AVERAGE TODO , currently only sum
        boolean skip();
        int getId();
        default int compareTo(Layer o) {
            return Integer.compare(this.getId(), o.getId());
        }
    }

}
