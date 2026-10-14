# Timezones

An app for keeping track of what time it is for your friends in different parts of the world :3

Yup, it's as simple as that!

> [!IMPORTANT]
> This app requires **Android 16 or later!**

## Installation

Go to the [latest release][latest] and download the `timezones.apk` file, or directly download it
through [this link][download] :3

After downloading the .apk file, open it on your phone and either the prompt to install, or first
the prompt to allow installing from your browser, should pop up :3

After downloading it, your browser should show an option to open the file, just use that, it's
quicker than finding and opening the file yourself later :3

## Usage

To get started, add a person you wanna know the timezone of. You can do so by heading to the People
tab and clicking the `+` button.

<img src="readme/01.png" width="400" alt="Select People Tab">

<img src="readme/02.png" width="400" alt="Click on Add Button">

The Add Person dialog will pop up.

<img src="readme/03.png" width="400" alt="Add Person Dialog">

Then you can add some information about the person. Required are the name of the person, as well as
their timezone and the name you wanna give that timezone. When you select a timezone without inputting
a name for the timezone first, the name will be autofilled, e.g. if you select America/New_York,
the name will be autofilled as "New York". After you're done, hit the Save button.

> [!Note]
> Multiple people can be in the same timezone, so don't name a timezone after a person!
>
> Also, as you might've guessed from the example, there's no spaces allowed in an IANA-Timezone ID,
> so spaces are replaced with underscores `_`. But don't worry, you don't need to know the exact
> IANA-Timezone ID to add it. There's a dropdown that uses the input as a search field, so typing
> for example 'America' will show only options containing 'America' in the dropdown.

<img src="readme/04.png" width="400" alt="Example Information">

After saving, you can see the new person you just added in the People tab, as well as their current
time.

<img src="readme/05.png" width="400" alt="New Person Added">

Now switch back to the Time tab.

<img src="readme/06.png" width="400" alt="Select Time Tab">

You'll now see the new timezone that you added.

<img src="readme/07.png" width="400" alt="New Timezone Added">

> [!Note]
> If the person you added is in the same timezone as you, this will look different!
>
> <img src="readme/08.png" width="400" alt="Person In Same Timezone">

Clicking on the person in the timezone (or in your time), or clicking on the person in the People
tab, opens the details dialog. Here you can see their name, nickname, timezone, as well as any
custom information you added.

<img src="readme/09.png" width="400" alt="Person Details">

To add custom information, in the Add Person or Edit Person dialog (Press 'Edit Details' in the
Details dialog), click the "Add Field" button.

<img src="readme/10.png" width="400" alt="Add Field button">

<img src="readme/11.png" width="400" alt="Field Inputs">

Then give the field a name and a value. Only the name is mandatory, a field without a value will be
marked with `-` as the value. This can be used to, for example, add birthdays, usernames for certain
platforms, or really anything you want! You can also use the "Remove Field" button to remove the
field again!

<img src="readme/12.png" width="400" alt="Example Field and Remove Field button">

In the Edit Person Dialog, you can also choose to delete a Person from the app or change the
Timezone. If there is a timezone that no people are using anymore, for example you delete the last
person that was using a timezone, or change their timezone to a different one, the unused timezone
is deleted.

You can add multiple People to the app though! And once you have one person, when you open the
Add Person dialog, the Timezone selection looks different! The first button opens a dropdown to
select a timezone that already-added people are already using, defaulting to the first (or only)
one. Clicking the "Create a new Timezone" button gives you the two text inputs again for the name
and the timezone ID, with a new button underneath to go back to choosing an existing timezone.

<img src="readme/13.png" width="400" alt="Choose existing timezone">

<img src="readme/14.png" width="400" alt="Create new timezone or choose existing">

Back in the Time tab, if you click on the time of a timezone, it opens the comparison dialog!

<img src="readme/15.png" width="400" alt="Click on the Time">

<img src="readme/16.png" width="400" alt="Comparison Dialog">

Here you can drag the slider around to compare your time to the chosen timezone's time, from
the last 12am/00:00 in the past/right now, to the next 12am/00:00 in the future, so 24 hours in
total, in 10 minute steps.

Don't like the order of the timezones? Switch to the People tab and tap the "Reorder timezones"
button!

<img src="readme/17.png" width="400" alt="Reorder timezones button">

This opens the Reorder Timezones dialog, where you can change the order of them. People are always
displayed in the same order, though, alphabetically.

<img src="readme/18.png" width="400" alt="Reorder timezones dialog">

Once you're done, hit Save, or hit Cancel to undo your changes and leave them as they were! Remember
that your own timezone is always hidden!

That's all!



<!-- Link Definitions -->
[latest]: https://github.com/CatMaxiMyoga/TimezonesAndroid/releases/latest
[download]: https://github.com/CatMaxiMyoga/TimezonesAndroid/releases/latest/download/timezones.apk