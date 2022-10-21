// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.Manifest;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import com.netease.yunxin.app.medical.R;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.databinding.ActivityAuthPreviewBinding;
import com.netease.yunxin.app.medical.utils.NavUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import java.io.IOException;
import java.util.List;

public class AuthPreviewActivity extends BaseActivity implements Handler.Callback {

  private static final int MSG_OPEN_CAMERA = 1;
  private static final int MSG_CLOSE_CAMERA = 2;
  private static final int MSG_SET_PREVIEW_SIZE = 3;
  private static final int MSG_SET_PREVIEW_SURFACE = 4;
  private static final int MSG_START_PREVIEW = 5;
  private static final int MSG_STOP_PREVIEW = 6;
  private static final int MSG_SET_PICTURE_SIZE = 7;
  private static final int MSG_SKIP_SUCCESS = 8;
  private static final int DELAY_TILE = 3000; // 停留三秒跳转到成功页面

  private static final String TAG = "AuthPreviewActivity";
  private static final String[] REQUIRED_PERMISSIONS = {
    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  private static final int PREVIEW_FORMAT = ImageFormat.NV21;

  private HandlerThread mCameraThread = null;

  private Handler mCameraHandler = null;

  private Camera.CameraInfo mFrontCameraInfo = null;
  private int mFrontCameraId = -1;

  private Camera.CameraInfo mBackCameraInfo = null;
  private int mBackCameraId = -1;

  private Camera mCamera;
  private int mCameraId = -1;
  private Camera.CameraInfo mCameraInfo;

  private SurfaceHolder mPreviewSurface;
  private int mPreviewSurfaceWidth;
  private int mPreviewSurfaceHeight;

  private DeviceOrientationListener mDeviceOrientationListener;

  private ActivityAuthPreviewBinding binding;

  @Override
  public boolean handleMessage(Message msg) {
    switch (msg.what) {
      case MSG_OPEN_CAMERA:
        {
          openCamera(msg.arg1);
          break;
        }
      case MSG_CLOSE_CAMERA:
        {
          closeCamera();
          break;
        }
      case MSG_SET_PREVIEW_SIZE:
        {
          int shortSide = msg.arg1;
          int longSide = msg.arg2;
          setPreviewSize(shortSide, longSide);
          break;
        }
      case MSG_SET_PREVIEW_SURFACE:
        {
          SurfaceHolder previewSurface = (SurfaceHolder) msg.obj;
          setPreviewSurface(previewSurface);
          break;
        }
      case MSG_START_PREVIEW:
        {
          startPreview();
          break;
        }
      case MSG_STOP_PREVIEW:
        {
          stopPreview();
          break;
        }
      case MSG_SET_PICTURE_SIZE:
        {
          int shortSide = msg.arg1;
          int longSide = msg.arg2;
          setPictureSize(shortSide, longSide);
          break;
        }
      case MSG_SKIP_SUCCESS:
        finish();
        NavUtils.toAuthSuccessPage(this);
        break;

      default:
        throw new IllegalArgumentException("Illegal message: " + msg.what);
    }
    return false;
  }

  @Override
  protected View getContentView() {
    binding = ActivityAuthPreviewBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected String getTitleContent() {
    return getString(R.string.medical_auth_name_title);
  }

  @Override
  protected void initView() {
    mDeviceOrientationListener = new DeviceOrientationListener(this);
    startCameraThread();
    initCameraInfo();
    binding.cameraPreview.getHolder().addCallback(new PreviewSurfaceCallback());
  }

  @Override
  protected void setEvent() {}

  @Override
  protected void onStart() {
    super.onStart();
    DeviceOrientationListener deviceOrientationListener = mDeviceOrientationListener;
    if (deviceOrientationListener != null) {
      deviceOrientationListener.enable();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (!isRequiredPermissionsGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      ToastX.showShortToast(getString(R.string.medical_permission_request_failed_tips));
      finish();
    } else if (mCameraHandler != null) {
      mCameraHandler.obtainMessage(MSG_OPEN_CAMERA, getCameraId(), 0).sendToTarget();
      mCameraHandler.sendEmptyMessageDelayed(MSG_SKIP_SUCCESS, DELAY_TILE);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mCameraHandler != null) {
      mCameraHandler.removeMessages(MSG_OPEN_CAMERA);
      mCameraHandler.sendEmptyMessage(MSG_CLOSE_CAMERA);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    DeviceOrientationListener deviceOrientationListener = mDeviceOrientationListener;
    if (deviceOrientationListener != null) {
      deviceOrientationListener.disable();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    stopCameraThread();
  }

  //获取要开启的相机 ID，优先开启前置。
  private int getCameraId() {
    if (hasFrontCamera()) {
      return mFrontCameraId;
    } else if (hasBackCamera()) {
      return mBackCameraId;
    } else {
      throw new RuntimeException("No available camera id found.");
    }
  }

  //判断我们需要的权限是否被授予，只要有一个没有授权，我们都会返回 false。
  private boolean isRequiredPermissionsGranted() {
    return PermissionUtils.hasPermissions(this, REQUIRED_PERMISSIONS);
  }

  private void startCameraThread() {
    mCameraThread = new HandlerThread("CameraThread");
    mCameraThread.start();
    mCameraHandler = new Handler(mCameraThread.getLooper(), this);
  }

  private void stopCameraThread() {
    if (mCameraThread != null) {
      mCameraThread.quitSafely();
    }
    mCameraThread = null;
    mCameraHandler = null;
  }

  //初始化摄像头信息。
  private void initCameraInfo() {
    int numberOfCameras = Camera.getNumberOfCameras(); // 获取摄像头个数
    for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
      Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
      Camera.getCameraInfo(cameraId, cameraInfo);
      if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
        // 后置摄像头信息
        mBackCameraId = cameraId;
        mBackCameraInfo = cameraInfo;
      } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        // 前置摄像头信息
        mFrontCameraId = cameraId;
        mFrontCameraInfo = cameraInfo;
      }
    }
  }

  /** 开启指定摄像头 */
  @WorkerThread
  private void openCamera(int cameraId) {
    Camera camera = mCamera;
    if (camera != null) {
      throw new RuntimeException("You must close previous camera before open a new one.");
    }
    if (PermissionUtils.hasPermissions(this, Manifest.permission.CAMERA)) {
      mCamera = Camera.open(cameraId);
      mCameraId = cameraId;
      mCameraInfo = cameraId == mFrontCameraId ? mFrontCameraInfo : mBackCameraInfo;
      Log.d(TAG, "Camera[" + cameraId + "] has been opened.");
      assert mCamera != null;
      mCamera.setDisplayOrientation(getCameraDisplayOrientation(mCameraInfo));
    }
  }

  /** 获取预览画面要校正的角度。 */
  private int getCameraDisplayOrientation(Camera.CameraInfo cameraInfo) {
    int rotation = getWindowManager().getDefaultDisplay().getRotation();
    int degrees = 0;
    switch (rotation) {
      case Surface.ROTATION_0:
        degrees = 0;
        break;
      case Surface.ROTATION_90:
        degrees = 90;
        break;
      case Surface.ROTATION_180:
        degrees = 180;
        break;
      case Surface.ROTATION_270:
        degrees = 270;
        break;
    }
    int result;
    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
      result = (cameraInfo.orientation + degrees) % 360;
      result = (360 - result) % 360; // compensate the mirror
    } else { // back-facing
      result = (cameraInfo.orientation - degrees + 360) % 360;
    }
    return result;
  }

  /** 关闭相机。 */
  @WorkerThread
  private void closeCamera() {
    Camera camera = mCamera;
    mCamera = null;
    if (camera != null) {
      camera.release();
      mCameraId = -1;
      mCameraInfo = null;
    }
  }

  /** 根据指定的尺寸要求设置预览尺寸，我们会同时考虑指定尺寸的比例和大小。 */
  @WorkerThread
  private void setPreviewSize(int shortSide, int longSide) {
    Camera camera = mCamera;
    if (camera != null && shortSide != 0 && longSide != 0) {
      float aspectRatio = (float) longSide / shortSide;
      Camera.Parameters parameters = camera.getParameters();
      List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
      for (Camera.Size previewSize : supportedPreviewSizes) {
        if ((float) previewSize.width / previewSize.height == aspectRatio
            && previewSize.height <= shortSide
            && previewSize.width <= longSide) {
          parameters.setPreviewSize(previewSize.width, previewSize.height);
          Log.d(
              TAG,
              "setPreviewSize() called with: width = "
                  + previewSize.width
                  + "; height = "
                  + previewSize.height);

          if (isPreviewFormatSupported(parameters, PREVIEW_FORMAT)) {
            parameters.setPreviewFormat(PREVIEW_FORMAT);
            int frameWidth = previewSize.width;
            int frameHeight = previewSize.height;
            int previewFormat = parameters.getPreviewFormat();
            PixelFormat pixelFormat = new PixelFormat();
            PixelFormat.getPixelFormatInfo(previewFormat, pixelFormat);
            int bufferSize = (frameWidth * frameHeight * pixelFormat.bitsPerPixel) / 8;
            camera.addCallbackBuffer(new byte[bufferSize]);
            camera.addCallbackBuffer(new byte[bufferSize]);
            camera.addCallbackBuffer(new byte[bufferSize]);
            Log.d(TAG, "Add three callback buffers with size: " + bufferSize);
          }

          camera.setParameters(parameters);
          break;
        }
      }
    }
  }

  /** 判断指定的预览格式是否支持。 */
  private boolean isPreviewFormatSupported(Camera.Parameters parameters, int format) {
    List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
    return supportedPreviewFormats != null && supportedPreviewFormats.contains(format);
  }

  /** 根据指定的尺寸要求设置照片尺寸，我们会考虑指定尺寸的比例，并且去符合比例的最大尺寸作为照片尺寸。 */
  @WorkerThread
  private void setPictureSize(int shortSide, int longSide) {
    Camera camera = mCamera;
    if (camera != null && shortSide != 0 && longSide != 0) {
      float aspectRatio = (float) longSide / shortSide;
      Camera.Parameters parameters = camera.getParameters();
      List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
      for (Camera.Size pictureSize : supportedPictureSizes) {
        if ((float) pictureSize.width / pictureSize.height == aspectRatio) {
          parameters.setPictureSize(pictureSize.width, pictureSize.height);
          camera.setParameters(parameters);
          Log.d(
              TAG,
              "setPictureSize() called with: width = "
                  + pictureSize.width
                  + "; height = "
                  + pictureSize.height);
          break;
        }
      }
    }
  }

  /** 设置预览 Surface。 */
  @WorkerThread
  private void setPreviewSurface(@Nullable SurfaceHolder previewSurface) {
    Camera camera = mCamera;
    if (camera != null && previewSurface != null) {
      try {
        camera.setPreviewDisplay(previewSurface);
        Log.d(TAG, "setPreviewSurface() called");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /** 开始预览。 */
  @WorkerThread
  private void startPreview() {
    Camera camera = mCamera;
    SurfaceHolder previewSurface = mPreviewSurface;
    if (camera != null && previewSurface != null) {
      camera.setPreviewCallbackWithBuffer(new PreviewCallback());
      camera.startPreview();
      Log.d(TAG, "startPreview() called");
    }
  }

  /** 停止预览。 */
  @WorkerThread
  private void stopPreview() {
    Camera camera = mCamera;
    if (camera != null) {
      camera.stopPreview();
      Log.d(TAG, "stopPreview() called");
    }
  }

  /**
   * 判断是否有后置摄像头。
   *
   * @return true 代表有后置摄像头
   */
  private boolean hasBackCamera() {
    return mBackCameraId != -1;
  }

  /**
   * 判断是否有前置摄像头。
   *
   * @return true 代表有前置摄像头
   */
  private boolean hasFrontCamera() {
    return mFrontCameraId != -1;
  }

  private class PreviewCallback implements Camera.PreviewCallback {
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
      // 在使用完 Buffer 之后记得回收复用。
      camera.addCallbackBuffer(data);
    }
  }

  private class DeviceOrientationListener extends OrientationEventListener {

    private DeviceOrientationListener(Context context) {
      super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {}
  }

  private class PreviewSurfaceCallback implements SurfaceHolder.Callback {
    @Override
    public void surfaceCreated(SurfaceHolder holder) {}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      mPreviewSurface = holder;
      mPreviewSurfaceWidth = width;
      mPreviewSurfaceHeight = height;
      Handler cameraHandler = mCameraHandler;
      if (cameraHandler != null) {
        cameraHandler.obtainMessage(MSG_SET_PREVIEW_SIZE, width, height).sendToTarget();
        cameraHandler.obtainMessage(MSG_SET_PICTURE_SIZE, width, height).sendToTarget();
        cameraHandler.obtainMessage(MSG_SET_PREVIEW_SURFACE, holder).sendToTarget();
        cameraHandler.sendEmptyMessage(MSG_START_PREVIEW);
      }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
      mPreviewSurface = null;
      mPreviewSurfaceWidth = 0;
      mPreviewSurfaceHeight = 0;
    }
  }
}
