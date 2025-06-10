# Android Ditto Data Migration App

## Overview
**Disclaimer:** NOT recommended for production use. Primarily to move development data over, use at your own risk!

This is a very simple app that can be used to migrate data from one app ID to another.

The app requires some minimal configuration and has a simple UI (two buttons and a log output) and is intended for data migration cases where there is a minimal amount of data to be transferred.

## Setup
To setup, add the following to your `local.properties` file and fill in the information from your Ditto portal:

```
# existing app information
existing_app_id=""
existing_playground_token=""
existing_websocket_url=""
existing_auth_url=""

# new app information
new_app_id=""
new_playground_token=""
new_websocket_url=""
new_auth_url=""
```

`existing_*` fields are the app you want to transfer FROM and `new_*` are the app you want to transfer TO.

## How to use
Once you have completed the setup step, build and run the app.

Tap "Start subscriptions" to subscribe to all your collections. This will effectively "download" all your data to the device.

The logs should state how many collections you're subscribing to and the disk store size will be displayed in the UI. This *should* match what you see in the "Collections" tab in the portal (eventually).

Once the data matches, tap the "Restart subscriptions with new app" button, which should then repeat the subscription process, but for the new app ID. Since we're using the same persistence directory, it should attempt to sync the local copy with the new app's big peer.