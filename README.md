# EHKeypad

![EHKeypad logo](https://raw.githubusercontent.com/dpolad/EHKeypad/master/doc/img/logo.png)
![Photo](https://raw.githubusercontent.com/dpolad/EHKeypad/master/doc/img/board.jpg)

## Description
EHKeypad is a device with the goal to enhance input capabilities of the smartphone adding physical inputs to it (eg buttons, switches, etc. ). 

The communication between the smartphone and EHKeypad is based on NFC. In addition, NFC allows the device to be entirely powered through energy harvesting.

In the proposed demo a keypad composed of 8 buttons, 8 switches and a potentiometer is used to control a music application on the smartphone.

* The 8 buttons are used to play a music synthesizer. 
* The 8 switches control a sequencer.
* The potentiometer controls the BPM of the sequencer.

The smartphone application gives a graphical interface to the device and lets the user configure sounds of both synthesizer and sequencer.

The project was developed by [Pola Davide](mailto:polish93@gmail.com) and [Barberis Enrico](mailto:enbarberis@gmail.com) for the [eSAME 2017 Conference - STM32Nucleo IoT Contest](http://www.esame-conference.org/program/stm32-iot-contest-2017/)

![Basic idea poster](https://raw.githubusercontent.com/dpolad/EHKeypad/master/doc/img/poster.jpg)

## Nucleo L476RG & X-NUCLEO-NFC04A1

In order to achieve the communication between the device and the smartphone, the "Fast transfer mode/Mailbox" of X-NUCLEO-NFC04A1 is used. In this mode, the board and the smartphone share a readable and writeable buffer of 256 bytes. In our project, the board continuously writes the buttons, switches and potentiometer status to this buffer. The smartphone, instead, continuously read this buffer.

In addition to it, the Energy Harvesting output provided by the X-NUCLEO-NFC04A1 board is used as power supply for both STM boards. 

All the information and datasheets can be found at these links:
- [Nucelo L476RG](http://www.st.com/en/evaluation-tools/nucleo-l476rg.html)
- [X-NUCLEO-NFC04A1](http://www.st.com/en/ecosystems/x-nucleo-nfc04a1.html)

### Pseudocode
To have a better comprehension of the behaviour of the board the following pseudocode can be useful:
```
init_GPIO();
init_ADC();
init_NFC();   //Mailbox & Energy Harvesting

while(1)
{
  msg[0] = read_buttons_status();
  msg[1] = read_switch_status();
  msg[2] = read_ADC_value();
  
  write_to_mailbox_buffer(msg);
  
  wait_smartphone_read_msg();
}

```
Notice that buttons and switches status are using only 2 bytes since every bit is used to signal the ON/OFF status.
The ADC value uses 1 byte since the value is between 0 and 255.

### Development Environment
The entire project was developed using Keil uVision 5 and the sources are available in the folder `nucelo_project`.
The missing libraries can be found at these links:
- [STM32Cube_FW_L4_V1.9.0](http://www.st.com/en/embedded-software/stm32cubel4.html)
- [STM32CubeExpansion_NFC4_V1.0.0_L476](https://mycore.core-cloud.net/index.php/s/03LiIbinnGYa6no)

## Android application
![App screenshots](https://raw.githubusercontent.com/dpolad/EHKeypad/master/doc/img/screenshot.png)

The developed Android application is composed of 3 frames:
- **LOG** : it shows all the messages received from the mailbox. In case of error it shows the corresponding error ID.
- **DEBUG** : this frame displays the status of the buttons, switches and ADC in a graphical way.
- **CONTROL** : this frame allows the user to compose music using as interface the physical inputs. In particular, is possible to assign the desired sound to each button from a pool of sounds. The same can be done for each of 4 sequencer lines.
