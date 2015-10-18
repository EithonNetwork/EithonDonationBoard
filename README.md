# EithonDonationBoard

## Release history

### 1.8 (2015-10-18)

* CHANGE: Refactoring EithonLibrary.

### 1.7 (2015-08-10)

* CHANGE: All time span configuration values are now in the general TimeSpan format instead of hard coded to seconds or minutes or hours.

### 1.6 (2015-07-23)

* NEW: Added a /donationboard playerstats command.

### 1.5.1 (2015-07-20)

* BUG: Now doesn't reset all plugin alarms when disabled.

### 1.5 (2015-07-18)

* NEW: New message for no change in perk level.

### 1.4 (2015-07-16)

* NEW: Added resetplayer command to reset player information.
* BUG: Bugfix for group New in zPermissons.
* BUG: Player was not always informed about perk updates
* BUG: Even if no change in perk level was done, the player could be informed. 

### 1.3 (2015-07-14)

* CHANGE: Added message to player when he/she needs to revisit the donation board.
* CHANGE: Added message to player when he/she has received a new perk level.

### 1.2.2 (2015-07-14)

* BUG: Players could get perks without going to the donation board.
* BUG: Perk levels was off by one.
* BUG: Exceptions logging in if no donation board exists.

### 1.2 (2015-07-08)

* CHANGE: Now uses EithonLibrary PermissionGroupLadder for permissions.
* BUG: New players lose their "New" rank when going to the DonationBoard.
* BUG: Now shows subcommands if no subcommand was given.

### 1.0 (2015-04-18)

* NEW: First Eithon release.


