emotool
===========

Tool for viewing and recording eeg data from [Emotiv EPOC EEG](http://www.emotiv.com) headset.

![Emotool Screenshot](http://i59.tinypic.com/33duyon.png)

emotool is built on top of [emokit-java](https://github.com/fommil/emokit-java)

running
===========
* `mvn compile`
* `mvn exec:java`

format
===========

Recorded data is saved in binary format where first 8 bytes is timestamp and next 32 bytes constitute data described here
https://github.com/openyou/emokit/blob/master/doc/emotiv_protocol.asciidoc

license
===========
Copyright (C) 2014 Joni Mikkola

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see http://www.gnu.org/licenses/.

thanks to
===========
* [Sam Halliday](http://github.com/fommil)
* [Cody Brocious](http://github.com/daeken)
* [Kyle Machulis](http://github.com/qdot)
* [Bill Schumacher](http://github.com/bschumacher)