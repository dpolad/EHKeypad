/**
  @page GPO Readme file
  
  @verbatim
  ******************** (C) COPYRIGHT 2017 STMicroelectronics *******************
  * @file    readme.txt 
  * @author  MMY Application Team
  * @version v1.0.0
  * @date    06-Mar-2017   
  * @brief   This example is for energy harvesting feature of ST25DV.
  ******************************************************************************
  *
  *
  * <h2><center>&copy; COPYRIGHT 2016 STMicroelectronics</center></h2>
  *
  * Licensed under ST MYLIBERTY SOFTWARE LICENSE AGREEMENT (the "License");
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at:
  *
  *        http://www.st.com/myliberty  
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
  * AND SPECIFICALLY DISCLAIMING THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *
  ******************************************************************************
  @endverbatim

@par Description

This directory contains a set of source files that implement a simple example based on 
ST25DV's GPO functionality

@note Care must be taken when using HAL_Delay(), this function provides accurate delay (in milliseconds)
      based on variable incremented in SysTick ISR. This implies that if HAL_Delay() is called from
      a peripheral ISR process, then the SysTick interrupt must have higher priority (numerically lower)
      than the peripheral interrupt. Otherwise the caller ISR process will be blocked.
      To change the SysTick interrupt priority you have to use HAL_NVIC_SetPriority() function.

@note The application need to ensure that the SysTick time base is always set to 1 millisecond
      to have correct HAL operation.

@par Directory contents 

  - main.h                   Main config file
  - stm32xxxx_hal_conf.h     Library Configuration file
  - stm32xxxx_it.h           Header for stm32xxxx_it.c
  - main.c                   Main program file
  - stm32xxxx_it.c           STM32xxxx Interrupt handlers
  - system_stm32xxxx.c       STM32xxxx system file

@par Hardware and Software environment  

  - This example runs on STM32L053R8 and STM32F401RE devices.
    
  - This application has been tested with STMicroelectronics:
    STM32L0xx-Nucleo RevC
    STM32F4xx-Nucleo RevC
    boards and can be easily tailored to any other supported device 
    and development board.

  - STM32LXxx-Nucleo and STM32FXxx-Nucleo RevC Set-up    
    - Connect the Nucleo board to your PC with a USB cable type A to mini-B 
      to ST-LINK connector (CN1).
    - Please ensure that the ST-LINK connector CN2 jumpers are fitted.

        
    
@par How to use it ? 

In order to make the program work, you must do the following :
 - Open your preferred toolchain 
 - Rebuild all files and load your image into target memory
 - Run the application

 * <h3><center>&copy; COPYRIGHT STMicroelectronics</center></h3>
 */
