#include <jni.h>

#include <stdio.h>

// for malloc
#if defined(_ALLBSD_SOURCE) || defined(__APPLE__)
#include <stdlib.h>
#else
#include <malloc.h>
#endif

#include "feedback_NativeAlgorithms.h"

JNIEXPORT jint JNICALL Java_feedback_NativeAlgorithms_load(JNIEnv *env, jobject obj) {
#ifdef __cplusplus
  return 1;
#else
  return 0;
#endif
}

#ifdef __cplusplus
static jintArray diff(JNIEnv *env, jint *good, int goodLength, jint *bad, int badLength) {
  int *memory = (int*) malloc(((goodLength + 3) * (badLength + 1) + (goodLength + badLength) ) * sizeof(int));
  int *opt = memory, *update = memory + (badLength + 1), *choices = memory + (badLength + 1) * 2;
  jint *path = memory + (goodLength + 3) * (badLength + 1);
  int *previous = choices, *current = choices;
  for (int i = 0; i <= badLength; i++) {
    opt[i] = 0;
    previous[i] = 1;
  }
  for (int j = 0; j < goodLength; j++) {
    const int e = good[j];
    current += badLength + 1;
    update[0] = opt[0];
    current[0] = 0;
    int opt_i = opt[0] + 1;
    int update_i = update[0];
    for (int i = 0; i < badLength; i++) {
        int opt_i_1 = opt[i + 1];
        int &best = update[i + 1];
        best = opt_i_1;
        int &choice = current[i + 1];
        choice = 0;
        if (e == bad[i] && best < opt_i && update_i < opt_i) {
            best = opt_i;
            choice = 2;
        }
        if (best < update_i) {
            best = update_i;
            choice = 1;
        }
        update_i = best;
        opt_i = opt_i_1 + 1;
    }
    int *tmp = update;
    update = opt;
    opt = tmp;
    previous = current;
  }
  int i = goodLength, j = badLength;
  const int len = goodLength + badLength - opt[badLength];
  path += len;
  while (i || j) {
    const jint choice = current[j];
    *(--path) = choice;
    if (choice != 0) {
      j--;
    }
    if (choice != 1) {
      current -= badLength + 1;
      i--;
    }
  }
  jintArray result = env->NewIntArray(len);
  if (result != NULL) {
    env->SetIntArrayRegion(result, 0, len, path);
  }
  free(memory);
  return result;
}
#endif

JNIEXPORT jintArray JNICALL Java_feedback_NativeAlgorithms_diff(JNIEnv *env, jobject obj, jintArray good, jint goodLength, jintArray bad, jint badLength) {
#ifdef __cplusplus
  jint *good_ = env->GetIntArrayElements(good, NULL);
  jint *bad_ = env->GetIntArrayElements(bad, NULL);
  jintArray result = diff(env, good_, goodLength, bad_, badLength);
  env->ReleaseIntArrayElements(good, good_, NULL);
  env->ReleaseIntArrayElements(bad, bad_, NULL);
  return result;
#endif
}
