<h1 dir="rtl">AparatView</h1>

<div dir="rtl">
آپارات یک سرویس اشتراک ویدیو محبوب در ایران است. این کتابخانه ساده اندروید برای نمایش ویدیو های آپارات درون برنامه در دو حالت عادی و تمام صفحه طراحی شده است.
</div>

<h2 dir="rtl">عکس ها</h2>

<div dir="rtl">

  <img 
    src="https://raw.githubusercontent.com/shahab-yousefi/aparat-view/main/screenshots/shot1.png"
    width="150"
    height="auto"
    alt="" />
  <img 
    src="https://raw.githubusercontent.com/shahab-yousefi/aparat-view/main/screenshots/shot2.png"
    width="150"
    height="auto"
    alt="" />
  <img 
    src="https://raw.githubusercontent.com/shahab-yousefi/aparat-view/main/screenshots/shot3.png"
    width="150"
    height="auto"
    alt="" />
  <img 
    src="https://raw.githubusercontent.com/shahab-yousefi/aparat-view/main/screenshots/shot4.png"
    width="auto"
    height="150"
    alt="" />

</div>

<h2 dir="rtl">نحوه نصب</h2>

[![](https://jitpack.io/v/codegamez/aparat-view.svg)](https://jitpack.io/#codegamez/aparat-view)

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

```gradle
dependencies {
    implementation 'com.github.codegamez:aparat-view:0.0.6'
}
```

<h2 dir="rtl">نحوه استفاده</h2>

```xml
<com.codegames.aparatview.AparatView
        android:layout_width="match_content"
        android:layout_height="wrap_content"
        app:aparatview_video_id="n1Sht"
        app:aparatview_video_ratio="16:9" />
```

<h2 dir="rtl">با تشکر از</h2>

[cprcrack/VideoEnabledWebView](https://github.com/cprcrack/VideoEnabledWebView)
