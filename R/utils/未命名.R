# 20250221
# author: benchen ye, xin liao

# Function: Defines a smoothing function smoothvec, used for smoothing the vector v
# paras：
# v: Input value vector.
# radius: Window size (normalized, ranging between 0 and 1)
# method: Smooth method, can be "mean" or "median".
smoothvec <- function(v, radius, method=c('mean', 'median')){
  stopifnot(is.vector(v))
  stopifnot(length(v) > 0)
  stopifnot(radius > 0 && radius < 1)
  
  halfwin <- ceiling(length(v) * radius)
  s <- rep(NA, length(v))
  
  for(i in 1:length(v)){
    winpos <- (i - halfwin) : (i + halfwin)
    winpos <- winpos[winpos > 0 & winpos <= length(v)]
    if(method == 'mean'){
      s[i] <- mean(v[winpos])
    }else if(method == 'median'){
      s[i] <- median(v[winpos])
    }
  }
  return(s)
}