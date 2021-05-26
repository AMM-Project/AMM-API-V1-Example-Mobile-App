### Disclaimer : this is not an official Sierra Wireless product

## To run Android Application
1. clone repo
2. In Android Studio, select import project - select p1APIDemo folder
3. Modify credentials in src/res/values/strings.xml are correct (see below)
4. Build project (gradle scripts should handle installing dependencies )
    - if that doesnt work, sync gradle and try again
    - ensure that you are using java 1.8
5. Create emulator through AVD Manager
    - min requied version is API 19.0 
    - Ex Pixel 3 API 24
6. Select emulator in Android Studio top bar and run it (green arrow on top bar)
7. Run Demo App from Run -> Run 'AppName'

## To Modify AMM API Client Credentials 

1. navigate to app/src/res/values/strings.xml
2. at the top of the page, modify "AMM_HOST_ADDRESS", "CLIENT_ID", "REDIRECT_URI" to appropriate values
3. (optional), modify "maps_api_key" 
