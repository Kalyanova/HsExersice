# exercise
Headspace Android Coding Exercise

**Android Developer Code Challenge**

The goal of this problem is to create an Android app that displays a list of photos from the picsum API ([https://picsum.photos/](https://picsum.photos/))

The endpoint you need to call is [https://picsum.photos/v2/list](https://picsum.photos/v2/list)

It **should** meet the following requirements:

- It should display the image, author, and dimensions of the photos
- These can be laid out in a vertical orientation or a grid
- It should handle the following states :
    - Empty State (no data)
    - Error State (api call failed)
    - Loading State (api call is taking place)
    - Content State (there is data to display)
- It should be functional while offline
- When loading a page of photos it should check to see if that page exists in the database, if it does then display those products otherwise make a call to the endpoint

**Skeleton App**

- Feel free to use the app skeleton provided here, to use your own or to start from scratch.
- The skeleton provided is setup with:
    - Common libraries: RxJava, Room, OkHttp, Retrofit, Gson, Glide, RecyclerView, ConstraintLayout
    - It uses the MVVM pattern
    - It provides some defaults providers for Network and Local DB calls
    - Skeleton classes for a feature: FeatureActivity, FeatureViewModel and FeatureTableDao

**Submission Instructions**

- Create a repo on your personal GitHub and send us the link.
- Avoid putting everything in a single commit.
- Track the time you took to complete the project.
- Add comments where appropriate
