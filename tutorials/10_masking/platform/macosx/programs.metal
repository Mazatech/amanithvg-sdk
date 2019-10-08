
#include <metal_stdlib>
#include <simd/simd.h>

using namespace metal;

// include header shared between this Metal shader code and C code executing Metal API commands
#import "ShaderTypes.h"

typedef struct {
    // the [[position]] attribute qualifier of this member indicates this value is the clip space
    // position of the vertex when this structure is returned from the vertex shader
    float4 position [[position]];
    float2 textureCoordinate;
} TexturedFragment;

// Vertex Function
vertex TexturedFragment texturedVertexShader(uint vertexID [[ vertex_id ]],
                                             constant TexturedVertex *vertexArray [[ buffer(0) ]],
                                             constant vector_uint2 *viewportSize [[ buffer(1) ]]) {

    TexturedFragment out;

    // index into the array of positions to get the current vertex (positions are specified in pixel dimensions)
    float2 pixelSpacePosition = vertexArray[vertexID].position.xy;
    // get the viewport size and cast to float
    float2 halfViewportSize = float2(*viewportSize) / 2.0;
    // convert pixel space positions to clip-space (Z is set to 0.0 and w to 1.0 because this is 2D sample)
    out.position = vector_float4(0.0, 0.0, 0.0, 1.0);
    out.position.xy = (pixelSpacePosition - halfViewportSize) / halfViewportSize;
    out.textureCoordinate = vertexArray[vertexID].textureCoordinate;
    return out;
}

// Fragment function
fragment float4 texturedFragmentShader(TexturedFragment in [[stage_in]],
                                       texture2d<float> texture [[ texture(0) ]]) {

    constexpr sampler textureSampler(mag_filter::nearest, min_filter::nearest, address::clamp_to_edge);
    // sample the texture to obtain a color
    const float4 colorSample = texture.sample(textureSampler, in.textureCoordinate);
    return colorSample;
}
