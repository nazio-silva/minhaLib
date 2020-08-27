package com.bulb.bulbs.beaconxpro.entity;

import java.io.Serializable;


public class BeaconXTLM implements Serializable {
    // Tensão da bateria
    public String vbatt;
    // Temperatura interna do chip
    public String temp;
    // Estatísticas de Broadcast Times
    public String adv_cnt;
    // Tempo de funcionamento do equipamento
    public String sec_cnt;
}
