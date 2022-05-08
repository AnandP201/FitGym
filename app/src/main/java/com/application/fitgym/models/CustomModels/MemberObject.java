package com.application.fitgym.models.CustomModels;

public class MemberObject {
    public String id,name,daysLeft,plans,memberSince,authID;
    public byte []b;

    public MemberObject(String I,String N,String D,String P,String A,String M,byte []c){
        this.id=I;
        this.name=N;
        this.daysLeft=D;
        this.plans=P;
        this.memberSince=M;
        this.authID=A;
        this.b=c;
    }


}
