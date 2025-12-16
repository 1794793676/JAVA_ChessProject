package com.xiangqi.shared.model;

/**
 * Enumeration of all Xiangqi piece types.
 */
public enum PieceType {
    GENERAL("将/帅"),      // General/King
    ADVISOR("士/仕"),      // Advisor/Guard  
    ELEPHANT("象/相"),     // Elephant/Bishop
    HORSE("马"),          // Horse/Knight
    CHARIOT("车"),        // Chariot/Rook
    CANNON("炮/砲"),      // Cannon
    SOLDIER("兵/卒");     // Soldier/Pawn
    
    private final String chineseName;
    
    PieceType(String chineseName) {
        this.chineseName = chineseName;
    }
    
    public String getChineseName() {
        return chineseName;
    }
}