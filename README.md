AwesAuth
========

This app is a demonstration of using [AppAuth for Android](https://github.com/openid/AppAuth-Android) and
[Google Sign-In for Android](https://developers.google.com/identity/sign-in/android/start) for OAuth2
and authentication.

### AppAuth for Android
The app uses Google OAuth2 configuration `google_config.json`. To run it on your own, you have to
create a configuration json as the `res/raw/xxx.json`
```json
{
  "client_id": "YOUR CLINET ID REGISTERED IN YOUR OAUTH2 PROVIDER",
  "redirect_uri": "XXX.XXX:/",
  "authorization_scope": "openid email profile",
  "discovery_uri": "YOUR OAUTH2 PROVIDER'S URI TO FETCH DISCOVERY DOCUMENT ",
  "authorization_endpoint_uri": "YOUR OAUTH2 PROVIDER'S ENDPOINT FOR AUTHORIZATION",
  "token_endpoint_uri": "YOUR OAUTH2 PROVIDER'S ENDPOINT FOR TOKEN",
  "registration_endpoint_uri": "xxx",
  "user_info_endpoint_uri": "YOUR OAUTH2 PROVIDER'S ENDPOINT FOR USER INFO",
  "https_required": true
}
```

> NOTE: `redirect_uri` is exclusive with `authorization_endpoint_uri`, `token_endpoint_uri` and
`user_info_endpoint_uri`

### Google Sign-In for Android
The only need to run it is to place you client id in `res/raw/google_signin.json` like this
```json
{
  "client_id": "YOUR CLINET ID REGISTERED IN YOUR OAUTH2 PROVIDER"
}
```

## License
Copyright Awesdroid 2018

This file is part of some open source application.

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

Contact: awesdroid@gmail.com