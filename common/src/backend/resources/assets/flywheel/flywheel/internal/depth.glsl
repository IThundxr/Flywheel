// d_l = \frac{ z_{near} \cdot z_{far} }{ z_{near} + d \cdot ( z_{far} - z_{near} ) }
float linearize_depth(float d, float zNear, float zFar) {
    return (zNear * zFar) / (zNear + d * (zFar - zNear));
}

// d = \frac{ z_{near} ( z_{far} - d_l ) }{ d_l ( z_{far} - z_{near} ) }
float delinearize_depth(float linearDepth, float zNear, float zFar) {
    return (zNear * (zFar - linearDepth)) / (linearDepth  * (zFar - zNear));
}
