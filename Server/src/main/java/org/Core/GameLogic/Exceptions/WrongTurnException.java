package org.Core.GameLogic.Exceptions;

public class WrongTurnException extends RuntimeException{
    public WrongTurnException(String message){
        super(message);
    }
}
