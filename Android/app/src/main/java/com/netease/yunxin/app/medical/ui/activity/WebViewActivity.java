// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.medical.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import com.netease.yunxin.app.medical.base.BaseActivity;
import com.netease.yunxin.app.medical.constant.AppParams;
import com.netease.yunxin.app.medical.databinding.ActivityWebviewBinding;

public class WebViewActivity extends BaseActivity {

  private static final String SCHEME_HTTP = "http";
  private static final String SCHEME_HTTPS = "https";

  private String title;
  private String url;

  private WebView webView;
  private ActivityWebviewBinding binding;

  @Override
  protected String getTitleContent() {
    title = getIntent().getStringExtra(AppParams.PARAM_KEY_TITLE);
    return title;
  }

  @Override
  protected View getContentView() {
    binding = ActivityWebviewBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    url = getIntent().getStringExtra(AppParams.PARAM_KEY_URL);
    webView = initWebView();
    ViewGroup webViewGroup = binding.rlRoot;
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    webView.setLayoutParams(layoutParams);
    webViewGroup.addView(webView);
    webView.loadUrl(url);
  }

  @Override
  protected void setEvent() {}

  private WebView initWebView() {
    WebView webView = new WebView(getApplicationContext());
    webView.setOnLongClickListener(v -> true);

    WebViewClient client =
        new WebViewClient() {

          @Override
          public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            boolean result =
                TextUtils.isEmpty(scheme)
                    || (!scheme.equals(SCHEME_HTTP) && !scheme.equals(SCHEME_HTTPS));
            if (result) {
              Intent intent = new Intent(Intent.ACTION_VIEW);
              intent.setData(uri);
              intent.addCategory(Intent.CATEGORY_DEFAULT);
              if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
              } else {
                result = false;
              }
            }
            return result;
          }
        };
    webView.setWebViewClient(client);
    webView.setWebChromeClient(new WebChromeClient());

    webView.getSettings().setUseWideViewPort(true);
    webView.getSettings().setLoadWithOverviewMode(true);

    webView.getSettings().setSupportZoom(true);
    webView.getSettings().setBuiltInZoomControls(true);
    webView.getSettings().setDisplayZoomControls(false);

    webView.getSettings().setDomStorageEnabled(true);
    webView.getSettings().setBlockNetworkImage(false);

    webView.getSettings().setJavaScriptEnabled(true);
    return webView;
  }

  @Override
  public void onBackPressed() {
    if (webView.canGoBack()) {
      webView.goBack();
      return;
    }
    super.onBackPressed();
  }
}
