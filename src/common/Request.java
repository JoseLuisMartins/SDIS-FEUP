package common;


import logic.ProtocolType;

import java.io.Serializable;

public class Request implements Serializable {
    private String operation;
    private String opnd1;
    private String opnd2;
    private boolean validRequest;

    public Request(String[] args) {
        this.validRequest=false;
        this.opnd1=null;
        this.opnd2=null;

        this.operation= args[1];



        switch (ProtocolType.valueOf(operation)){
            case BACKUP:
                if(args.length == 4) {
                    validRequest = true;
                    opnd1 = args[2];//file path
                    opnd2 = args[3]; // replication degree
                }
                break;
            case RESTORE:
                if(args.length == 3) {
                    validRequest = true;
                    opnd1=args[2];//file path
                }
            case DELETE:
                if(args.length == 3) {
                    validRequest = true;
                    opnd1=args[2];//file path
                }
            case RECLAIM:
                if(args.length == 3) {
                    validRequest = true;
                    opnd1=args[2];//reclaimed space (Kb)
                }

                break;
            default:
                break;
        }

    }

    public String getOpnd1() {
        return opnd1;
    }

    public int getReplication() {
        return Integer.parseInt(opnd2);
    }

    public String getOperation() {
        return operation;
    }

    public boolean isValid(){
        return validRequest;
    }

    @Override
    public String toString() {
        return "Request{" +
                "operation='" + operation + '\'' +
                ", opnd1='" + opnd1 + '\'' +
                ", opnd2='" + opnd2 + '\'' +
                ", validRequest=" + validRequest +
                '}';
    }
}
