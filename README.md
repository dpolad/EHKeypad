# EHKeypad

![EHKeypad logo](https://raw.githubusercontent.com/dpolad/EHKeypad/master/doc/img/logo.png)

## Description
The goal of EHKeypad is to enhance input capabilities of the smartphone adding physical inputs to it (eg buttons, switches, etc. ). 

The communication between the smartphone and EHKeypad is based on NFC. In addition, NFC allows the device to be entirely powered through energy harvesting.

In the proposed demo a keypad composed of 8 buttons, 8 switches and a potentiometer is used to control a music application on the smartphone.

* The 8 buttons are used to play a music synthesizer. 
* The 8 switches control a sequencer.
* The potentiometer controls the BPM of the sequencer.

The smartphone application gives a graphical interface to the device and lets the user configure the sounds of both synthesizer and sequencer.

In order to achieve the communication between the device and the smartphone, the "Fast transfer mode/Mailbox" is used.

The project was developed for the eSAME 2017 Conference - [STM32Nucleo IoT Contest](http://www.esame-conference.org/program/stm32-iot-contest-2017/)

## Development
![Basic idea poster](https://raw.githubusercontent.com/dpolad/EHKeypad/master/doc/img/poster.png)

### Nucleo L476RG

### Android application
![Basic idea poster](https://raw.githubusercontent.com/dpolad/EHKeypad/master/doc/img/screenshot.png)
