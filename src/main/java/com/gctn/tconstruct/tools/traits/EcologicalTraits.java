package com.gctn.tconstruct.tools.traits;

public class EcologicalTraits extends AbstractTraits{
    public EcologicalTraits(int level){
        super("ecological", 1, -1, 0xff8e661b);
        super.setLevel(level);
    }
    public int addLevel(int level){
        return this.level;
    }
    public int mergeLevel(int level){
        return this.level;
    }
}
