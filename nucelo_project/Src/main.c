#include "main.h"

/*
==============
=== PINOUT ===
==============

	-Button 0: PB13
	-Button 1: PB14
	-Button 2: PB15
	-Button 3: PB1
	-Button 4: PB2
	-Button 5: PB11
	-Button 6: PB12
	-Button 7: PA11
	
	-Switch 0: PA12
	-Switch 1: PC5
	-Switch 2: PC6
	-Switch 3: PC8
	-Switch 4: PA9
	-Switch 5: PC7
	-Switch 6: PB6
	-Switch 7: PA7
	
	-ADC: PA4
*/

/* Global variables ----------------------------------------------------------*/
uint8_t mailbox_msg[256];
uint16_t mblength;
uint8_t ITSTStatus;
uint8_t wait;
__IO uint16_t uhADCxConvertedData = VAR_CONVERTED_DATA_INIT_VALUE; /* Variables for ADC conversion data */

/* Function prototypes -----------------------------------------------*/
void GPIO_init( void );
void ErrorHandler( void );
void Activate_ADC(void);
void Configure_ADC(void);
void ConversionStartPoll_ADC_GrpRegular(void);

int main( void )
{
  ST25DV_MB_CTRL_DYN_STATUS mbctrldynstatus;
  ST25DV_PASSWD passwd;
  ST25DV_I2CSSO_STATUS i2csso;
  
	// Init HAL and leds
  HAL_Init( );
	NFC04A1_LED_Init( );
	
	/* Init GPIO */	
	GPIO_init();
	
	/* ADC Init */
	Configure_ADC();
  Activate_ADC();

  /* Turn on green led to show that configuration is started */
  NFC04A1_LED_ON( GREEN_LED );
  HAL_Delay( 300 );

	/* Init GPIO to receive GPO pin from ST25DV on NFC04A1 board */
	NFC04A1_GPO_Init(); //PIN A6
	
  /* Init ST25DV driver */
  while( BSP_NFCTAG_Init( ) != NFCTAG_OK );
   
	/* You need to present password before changing static configuration */
	BSP_NFCTAG_GetExtended_Drv()->ReadI2CSecuritySession_Dyn( &i2csso );
	if( i2csso == ST25DV_SESSION_CLOSED )
	{
		/* if I2C session is closed, present password to open session */
		passwd.MsbPasswd = 0; /* Default value for password */
		passwd.LsbPasswd = 0; /* change it if password has been modified */
		BSP_NFCTAG_GetExtended_Drv()->PresentI2CPassword( passwd );
	}	
	/* Set GPO Configuration, This is also copied into GPO dynamic */
	BSP_NFCTAG_ConfigIT( ST25DV_GPO_ENABLE_MASK | ST25DV_GPO_RFPUTMSG_MASK | ST25DV_GPO_RFGETMSG_MASK );
	
	/* Seg GPO pulse duration to 302 us */
	BSP_NFCTAG_GetExtended_Drv( )->WriteITPulse(ST25DV_302_US);
	
	/* Disable mailbox watchdog */
	BSP_NFCTAG_GetExtended_Drv( )->WriteMBWDG(0);
	
	/* Enable Mailbox */
	BSP_NFCTAG_GetExtended_Drv( )->WriteMBMode( ST25DV_ENABLE );
		
	/* Close session as dynamic register doesn't need open session for modification */
	passwd.MsbPasswd = 123;
	passwd.LsbPasswd = 123;
	BSP_NFCTAG_GetExtended_Drv()->PresentI2CPassword( passwd );
	
  /* Reset Mailbox in dynamique register */
	BSP_NFCTAG_GetExtended_Drv( )->ResetMBEN_Dyn( );
  BSP_NFCTAG_GetExtended_Drv( )->SetMBEN_Dyn( );

	/* Enable interrupt */
  HAL_NVIC_SetPriority( NFC04A1_GPO_EXTI, 0, 1 );
  HAL_NVIC_EnableIRQ( NFC04A1_GPO_EXTI );
	
	/* Init done */
  NFC04A1_LED_OFF( GREEN_LED );
  HAL_Delay( 300 );
		
	while(1)
	{
		
		/* Read button status */
		mailbox_msg[0] = 0;
				
		if (HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_13) == GPIO_PIN_RESET){
			mailbox_msg[0] |= 0x01;
		}
		if (HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_14) == GPIO_PIN_RESET){
			mailbox_msg[0] |= 0x02;
		}
		if (HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_15) == GPIO_PIN_RESET){
			mailbox_msg[0] |= 0x04;
		}
		if (HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_1) == GPIO_PIN_RESET){
			mailbox_msg[0] |= 0x08;
		}
		if (HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_2) == GPIO_PIN_RESET){
			mailbox_msg[0] |= 0x10;
		}
		if (HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_11) == GPIO_PIN_RESET){
			mailbox_msg[0] |= 0x20;
		}
		if (HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_12) == GPIO_PIN_RESET){
			mailbox_msg[0] |= 0x40;
		}
		if (HAL_GPIO_ReadPin(GPIOA, GPIO_PIN_11) == GPIO_PIN_RESET){
			mailbox_msg[0] |= 0x80;
		}
		
		
		/* Read switch status */
		mailbox_msg[1] = 0;
				
		if (HAL_GPIO_ReadPin(GPIOA, GPIO_PIN_12) == GPIO_PIN_RESET){
			mailbox_msg[1] |= 0x01;
		}
		if (HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_5) == GPIO_PIN_RESET){
			mailbox_msg[1] |= 0x02;
		}
		if (HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_6) == GPIO_PIN_RESET){
			mailbox_msg[1] |= 0x04;
		}
		if (HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_8) == GPIO_PIN_RESET){
			mailbox_msg[1] |= 0x08;	
		}
		if (HAL_GPIO_ReadPin(GPIOA, GPIO_PIN_9) == GPIO_PIN_RESET){
			mailbox_msg[1] |= 0x10;
		}
		if (HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_7) == GPIO_PIN_RESET){
			mailbox_msg[1] |= 0x20;
		}
		if (HAL_GPIO_ReadPin(GPIOB, GPIO_PIN_6) == GPIO_PIN_RESET){
			mailbox_msg[1] |= 0x40;
		}
		if (HAL_GPIO_ReadPin(GPIOA, GPIO_PIN_7) == GPIO_PIN_RESET){
			mailbox_msg[1] |= 0x80;	
		}		
		
				
		/* Read ADC value */
    ConversionStartPoll_ADC_GrpRegular();
    uhADCxConvertedData = LL_ADC_REG_ReadConversionData12(ADC1);
		mailbox_msg[2] = uhADCxConvertedData>>4;		
		
		/* If MB is available, write data */
		BSP_NFCTAG_GetExtended_Drv()->ReadMBctrl_Dyn( &mbctrldynstatus );
		while ( (mbctrldynstatus.HostPutMsg != 0) || (mbctrldynstatus.RfPutMsg != 0) )
		{
			BSP_NFCTAG_GetExtended_Drv()->ReadMBctrl_Dyn( &mbctrldynstatus );
		}
		
		/* Ok, buffer ready. Write to it! */
		wait = 1;
		BSP_NFCTAG_GetExtended_Drv()->WriteMailboxData( mailbox_msg, 3 );
				
		/* Wait RF device */
		while(wait);
		
		NFC04A1_LED_OFF( GREEN_LED );
	}
}

void HAL_GPIO_EXTI_Callback(uint16_t GPIO_Pin)
{  
  if(GPIO_Pin == NFC04A1_GPO_PIN)
  {
		BSP_NFCTAG_GetExtended_Drv()->ReadITSTStatus_Dyn(&ITSTStatus);
		/* Bit 0x40 = RF_GET_MSG */
		if( ITSTStatus & 0x40 ){
			wait = 0;
		}
  }
}

void GPIO_init( void ){
	GPIO_InitTypeDef   GPIO_InitStructure;
		
	// Enable clocks
	__HAL_RCC_GPIOA_CLK_ENABLE();
	__HAL_RCC_GPIOB_CLK_ENABLE();
	__HAL_RCC_GPIOC_CLK_ENABLE();
	
	// Port A
  GPIO_InitStructure.Mode = GPIO_MODE_INPUT;
  GPIO_InitStructure.Pull = GPIO_NOPULL;
  GPIO_InitStructure.Pin = GPIO_PIN_11 | GPIO_PIN_12 |
													 GPIO_PIN_9  | GPIO_PIN_7;
  HAL_GPIO_Init(GPIOA, &GPIO_InitStructure);
	
	// Port B
  GPIO_InitStructure.Mode = GPIO_MODE_INPUT;
  GPIO_InitStructure.Pull = GPIO_NOPULL;
  GPIO_InitStructure.Pin = GPIO_PIN_13 | GPIO_PIN_14 | GPIO_PIN_15 | GPIO_PIN_1 |
													 GPIO_PIN_2  | GPIO_PIN_11 | GPIO_PIN_12 |
													 GPIO_PIN_6;
  HAL_GPIO_Init(GPIOB, &GPIO_InitStructure);
	
	// Port C
  GPIO_InitStructure.Mode = GPIO_MODE_INPUT;
  GPIO_InitStructure.Pull = GPIO_NOPULL;
  GPIO_InitStructure.Pin = 	GPIO_PIN_5 | GPIO_PIN_6 | GPIO_PIN_8 | 
														GPIO_PIN_7;
  HAL_GPIO_Init(GPIOC, &GPIO_InitStructure);
}

void ErrorHandler( void )
{
		NFC04A1_LED_ON( BLUE_LED );
		while(1);
}

void Configure_ADC(void)
{
  LL_AHB2_GRP1_EnableClock(LL_AHB2_GRP1_PERIPH_GPIOA);
  LL_GPIO_SetPinMode(GPIOA, LL_GPIO_PIN_4, LL_GPIO_MODE_ANALOG);
  LL_GPIO_EnablePinAnalogControl(GPIOA, LL_GPIO_PIN_4);
  NVIC_SetPriority(ADC1_2_IRQn, 0);
  NVIC_EnableIRQ(ADC1_2_IRQn);
  LL_AHB2_GRP1_EnableClock(LL_AHB2_GRP1_PERIPH_ADC);
	
  if(__LL_ADC_IS_ENABLED_ALL_COMMON_INSTANCE() == 0)
  {
    LL_ADC_SetCommonClock(__LL_ADC_COMMON_INSTANCE(ADC1), LL_ADC_CLOCK_SYNC_PCLK_DIV2);
  }
	
  if ((LL_ADC_IsEnabled(ADC1) == 0)               ||
      (LL_ADC_REG_IsConversionOngoing(ADC1) == 0)   )
  {
    LL_ADC_REG_SetTriggerSource(ADC1, LL_ADC_REG_TRIG_SOFTWARE);
    LL_ADC_REG_SetContinuousMode(ADC1, LL_ADC_REG_CONV_SINGLE);
    LL_ADC_REG_SetOverrun(ADC1, LL_ADC_REG_OVR_DATA_OVERWRITTEN);
    LL_ADC_REG_SetSequencerLength(ADC1, LL_ADC_REG_SEQ_SCAN_DISABLE);
		
		LL_ADC_SetOverSamplingScope(ADC1, LL_ADC_OVS_GRP_REGULAR_CONTINUED);
		LL_ADC_ConfigOverSamplingRatioShift(ADC1, LL_ADC_OVS_RATIO_64, LL_ADC_OVS_SHIFT_RIGHT_6);
		
    LL_ADC_REG_SetSequencerRanks(ADC1, LL_ADC_REG_RANK_1, LL_ADC_CHANNEL_9);
  }

  if ((LL_ADC_IsEnabled(ADC1) == 0)                    ||
      ((LL_ADC_REG_IsConversionOngoing(ADC1) == 0) &&
       (LL_ADC_INJ_IsConversionOngoing(ADC1) == 0)   )   )
  {
    LL_ADC_SetChannelSamplingTime(ADC1, LL_ADC_CHANNEL_9, LL_ADC_SAMPLINGTIME_2CYCLES_5);
  }
  
  LL_ADC_EnableIT_OVR(ADC1);
}


void Activate_ADC(void)
{
  __IO uint32_t wait_loop_index = 0;
  #if (USE_TIMEOUT == 1)
  uint32_t Timeout = 0; /* Variable used for timeout management */
  #endif /* USE_TIMEOUT */
  
  if (LL_ADC_IsEnabled(ADC1) == 0)
  {
    LL_ADC_DisableDeepPowerDown(ADC1);
    LL_ADC_EnableInternalRegulator(ADC1);
    wait_loop_index = ((LL_ADC_DELAY_INTERNAL_REGUL_STAB_US * (SystemCoreClock / (100000 * 2))) / 10);
    while(wait_loop_index != 0)
    {
      wait_loop_index--;
    }
    LL_ADC_StartCalibration(ADC1, LL_ADC_SINGLE_ENDED);
    #if (USE_TIMEOUT == 1)
    Timeout = ADC_CALIBRATION_TIMEOUT_MS;
    #endif /* USE_TIMEOUT */
    
    while (LL_ADC_IsCalibrationOnGoing(ADC1) != 0)
    {
    #if (USE_TIMEOUT == 1)
      if (LL_SYSTICK_IsActiveCounterFlag())
      {
        if(Timeout-- == 0)
        {
					ErrorHandler();
        }
      }
    #endif /* USE_TIMEOUT */
    }
    
    wait_loop_index = (ADC_DELAY_CALIB_ENABLE_CPU_CYCLES >> 1);
    while(wait_loop_index != 0)
    {
      wait_loop_index--;
    }

    LL_ADC_Enable(ADC1);
    
    #if (USE_TIMEOUT == 1)
    Timeout = ADC_ENABLE_TIMEOUT_MS;
    #endif /* USE_TIMEOUT */
    
    while (LL_ADC_IsActiveFlag_ADRDY(ADC1) == 0)
    {
    #if (USE_TIMEOUT == 1)
      if (LL_SYSTICK_IsActiveCounterFlag())
      {
        if(Timeout-- == 0)
        {
					ErrorHandler();
        }
      }
    #endif /* USE_TIMEOUT */
    }
  }  
}

void ConversionStartPoll_ADC_GrpRegular(void)
{
  #if (USE_TIMEOUT == 1)
  uint32_t Timeout = 0; /* Variable used for timeout management */
  #endif /* USE_TIMEOUT */

  if ((LL_ADC_IsEnabled(ADC1) == 1)               &&
      (LL_ADC_IsDisableOngoing(ADC1) == 0)        &&
      (LL_ADC_REG_IsConversionOngoing(ADC1) == 0)   )
  {
    LL_ADC_REG_StartConversion(ADC1);
  }
  else
  {
    ErrorHandler();
  }
  
  #if (USE_TIMEOUT == 1)
  Timeout = ADC_UNITARY_CONVERSION_TIMEOUT_MS;
  #endif /* USE_TIMEOUT */
  
  while (LL_ADC_IsActiveFlag_EOC(ADC1) == 0)
  {
  #if (USE_TIMEOUT == 1)
    if (LL_SYSTICK_IsActiveCounterFlag())
    {
      if(Timeout-- == 0)
      {
				ErrorHandler();
      }
    }
  #endif /* USE_TIMEOUT */
  }
  
  LL_ADC_ClearFlag_EOC(ADC1);
}

void AdcGrpRegularOverrunError_Callback(void)
{
  LL_ADC_DisableIT_OVR(ADC1);
  ErrorHandler();
}
