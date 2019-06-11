#include <jni.h>
#include <string>

// C++调用C的函数
extern "C" {
extern int patch_main(int argc, const char *argv[]);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ff_bsdiff_BsPatch_patch(JNIEnv *env, jclass type, jstring oldFile_, jstring newFile_,
                                 jstring patchFile_) {
    const char *oldFile = env->GetStringUTFChars(oldFile_, nullptr);
    const char *newFile = env->GetStringUTFChars(newFile_, nullptr);
    const char *patchFile = env->GetStringUTFChars(patchFile_, nullptr);

    // 第一个参数并没有用到
    const char *argv[] = {"", oldFile, newFile, patchFile};


    patch_main(4, argv);

    env->ReleaseStringUTFChars(oldFile_, oldFile);
    env->ReleaseStringUTFChars(newFile_, newFile);
    env->ReleaseStringUTFChars(patchFile_, patchFile);
}