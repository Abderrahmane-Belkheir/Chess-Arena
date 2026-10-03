package org.Core.Game.Logic.Exceptions;

public class WrongTurnException extends RuntimeException{
    public WrongTurnException(String message){
        super(message);
    }
}
