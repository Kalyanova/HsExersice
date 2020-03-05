# exercise
Headspace Android Coding Exercise

**Android Developer Code Challenge**

The goal was to create an Android app that displays a list of photos from the picsum API ([https://picsum.photos/]
(https://picsum.photos/))

Demo of the resulting app (gif):

<img src="https://user-images.githubusercontent.com/9308897/75943624-907e4d80-5ea6-11ea-8eb7-c17378fef5dc.gif" width="25%" alt="Главный экран в ландшафтном режиме"/>

The app meets the following requirements:

- It displays the image, author, and dimensions of the photos
- Photos are laid out in a vertical orientation
- It handles the following states :
    - Empty State (no data)
    - Error State (api call failed)
    - Loading State (api call is taking place)
    - Content State (there is data to display)
- Besides, the app checks whether network connection is available again after connection loss and if it is,
  it is trying to reload data
- It is functional while offline (if data were uploaded into the database before, the data are available).
- When loading a page of photos it checks if that page exists in the database, and if it does then the app displays
those photos from database, rather than making a call to the endpoint via network
