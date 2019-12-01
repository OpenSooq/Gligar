<a href="https://opensooq.com/">
    <img src="https://opensooqui2.os-cdn.com/os_web/desktop/opensooq-logo.svg" alt="OpenSooq logo" title="OpenSooq" align="right" height="70" />
</a>

Gliger [Android Image Picker Library]
======================
![API](https://img.shields.io/badge/100%25-Kotlin-brightgreen) ![API](https://img.shields.io/badge/API-17%2B-green.svg?style=flat) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)   [ ![Download](https://api.bintray.com/packages/opensooq/Gligar/gligar/images/download.svg?version=1.0.0) ](https://bintray.com/opensooq/Gligar/gligar/1.0.0/link)

:star: Star us on GitHub — it helps!

[![forthebadge](https://forthebadge.com/images/badges/built-with-love.svg)](https://forthebadge.com)

##### Gliger is an Easy, lightweight and high performance image picker library for Android!
##### Gliger load images using Android content resolver with the help of coroutines to lazy load the images and improve the performance!
##### Gliger handle permission requests, support camera capture, and limit for the max number of images to pick. 



Demo
======================
<img src="https://github.com/OpenSooq/Gligar/blob/master/demo.gif"  height="440" />

## Table of content

* [Download](#download)
* [Sample Project](#sample-project)
* [Usage](#usage)
* [UI customization](#UI-customization)
- [License](#license)

# Download

This library is available in **jCenter** which is the default Maven repository used in Android Studio. You can also import this library from source as a module.
 
```groovy
dependencies {
    // other dependencies here
    implementation 'com.opensooq.supernova:gligar:1.0.0'
}
```


# Sample Project
We have a sample project demonstrating how to use the library.

Checkout the demo  [here](https://github.com/OpenSooq/Gligar/tree/master/app/src/main/java/com/opensooq/supernova/gligarexample)



# Usage
#### Simpler than ever!

To open the Picker: 

### Kotlin
```kotlin
Gligar().requestCode(PICKER_REQUEST_CODE).withActivity(this).show()
```
### Java

```java
new Gligar().requestCode(PICKER_REQUEST_CODE).withActivity(this).show();
```

To get the result override onActivityResult method: 

### Kotlin
```kotlin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            PICKER_REQUEST_CODE -> {
                val imagesList = data?.extras?.getStringArray(Gligar.IMAGES_RESULT)// return list of selected images paths.
                if (!imagesList.isNullOrEmpty()) {
                    imagesCount.text = "Number of selected Images: ${imagesList.size}"
                }
            }
        }
    }
```

### Java

```java
@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode){
            case PICKER_REQUEST_CODE : {
              String pathsList[]= data.getExtras().getStringArray(Gligar.IMAGES_RESULT); // return list of selected images paths.
                imagesCount.text = "Number of selected Images: " + pathsList.length;
                break;
            }   
        }
    }
```


| Method | usage |
| ------ | ------ |
| ``` withActivity(activity: Activity) ``` | used to set the activity will recive the result  |
| ``` withFragment(fragment: Fragment) ``` | used to set the fragment will recive the result  |
| ``` requestCode(requestCode: Int) ``` | used to set the request code for the result  |
| ``` limit(limit: Int) ``` | used to set the max number of images to select  |
| ``` disableCamera(disableCamera: Boolean) ``` | by default the value of disableCamera is false, it determine to allow camera captures or not  |
| ``` cameraDirect(cameraDirect: Boolean) ``` | by default the value of cameraDirect is false, it determine to open the camera before showing the picker or not |


## UI customization

### Customizable Colors
override any color to change inside your project colors.xml

|		Property		|		Description		|
|:----------------------|:---------------------:|
|		`counter_bg`			|	selection counter background color			|
|		`counter_color`		|	selection counter text color	|
|		`selector_color`		|	selection tint color	|
|		`place_holder_color`		|	empty image holder color	|
|		`ripple_color`		|	selection ripple color	|
|		`toolbar_bg`		|	toolbar color	|
|		`done`		|	Done button color	|
|		`change_album_color`		|	change album text color	|
|		`camera_icon_color`		|	camera icon color	|
|		`camera_text_color`		|	camera text color	|
|		`permission_alert_bg`		|	permission alert background color	|
|		`permission_alert_color`		|	permission alert text color	|



### Customizable Texts
override any string to change inside your project strings.xml

|		Property		|		Description		|
|:----------------------|:---------------------:|
|		`over_limit_msg`			|	selection limit reached message			|
|		`capture_new_image`		|	capture image text	|
|		`change_album_text`		|	change album text	|
|		`permission_storage_never_ask`		|	permission denied message 	|
|		`all_images`		|	all images album name	|




# License

```
Copyright 2019 OpenSooq

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
