package ru.codebattle.client;

import ru.codebattle.client.api.*;

import java.net.URISyntaxException;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;

public class LodeRunnerClient extends LoderunnerBase {

    private Function<GameBoard, LoderunnerAction> callback;
    private final Runnable closeHandler;

    public LodeRunnerClient(String url, Runnable closeHandler) throws URISyntaxException {
        super(url);
        this.closeHandler = closeHandler;
    }
    public void run(Function<GameBoard, LoderunnerAction> callback) {
        connect();
        this.callback = callback;
    }

    @Override
    protected String doMove(GameBoard gameBoard) {
        BoardPoint currentBoardPoint = gameBoard.getMyPosition();
        GoldFinder goldFinder = new GoldFinder(currentBoardPoint, gameBoard);
        return loderunnerActionToString(goldFinder.findAction());
    }


    @Override
    public void onClose(int code, String reason, boolean remote) {
        super.onClose(code, reason, remote);
        closeHandler.run();
    }

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void initiateExit() {
        setShouldExit(true);
    }
}
