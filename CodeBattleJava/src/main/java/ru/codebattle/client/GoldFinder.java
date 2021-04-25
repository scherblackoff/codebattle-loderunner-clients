package ru.codebattle.client;

import jdk.swing.interop.SwingInterOpUtils;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.ls.LSOutput;
import ru.codebattle.client.api.BoardElement;
import ru.codebattle.client.api.BoardPoint;
import ru.codebattle.client.api.GameBoard;
import ru.codebattle.client.api.LoderunnerAction;

import java.util.*;


public class GoldFinder {

    @Getter
    private final BoardPoint currentBoardPoint;
    @Getter
    private final GameBoard currentGameBoard;
    @Getter
    private int smallestWay;
    @Getter
    private boolean isFind;

    private final Set<BoardPoint> visitedPoints = new HashSet<>();
    private static int counter;

    private LoderunnerAction DEFAULT_loderunnerAction = LoderunnerAction.DO_NOTHING;

    private final Queue<BoardPointWithAction> boardPoints;

    public GoldFinder(BoardPoint currentBoardPoint, GameBoard currentGameBoard) {
        this.currentBoardPoint = currentBoardPoint;
        this.currentGameBoard = currentGameBoard;
        boardPoints = new ArrayDeque<>();
        BoardPointWithAction boardPointWithActionNow = new BoardPointWithAction(currentBoardPoint);
        visitedPoints.add(boardPointWithActionNow.getBoardPoint());
        BoardPointWithAction boardPointWithActionLeft = new BoardPointWithAction(currentBoardPoint.shiftLeft(), LoderunnerAction.GO_LEFT);
        if (canGoLeft(boardPointWithActionNow)) {
            boardPoints.add(boardPointWithActionLeft);
        }
        BoardPointWithAction boardPointWithActionRight =
                new BoardPointWithAction(currentBoardPoint.shiftRight(), LoderunnerAction.GO_RIGHT);
        if (canGoRight(boardPointWithActionNow)) {
            boardPoints.add(boardPointWithActionRight);
        }
        BoardPointWithAction boardPointWithActionUp =
                new BoardPointWithAction(currentBoardPoint.shiftTop(), LoderunnerAction.GO_UP);
        if (canGoUp(boardPointWithActionNow)) {
            boardPoints.add(boardPointWithActionUp);
        }
        BoardPointWithAction boardPointWithActionDown =
                new BoardPointWithAction(currentBoardPoint.shiftBottom(), LoderunnerAction.GO_DOWN);
        if (canGoDown(boardPointWithActionNow)) {
            boardPoints.add(boardPointWithActionDown);
        }
        /*if (checkBottomBlock(boardPointWithActionRight)) {
            boardPoints.add(new BoardPointWithAction(boardPointWithActionRight.getBoardPoint().shiftBottom(), LoderunnerAction.DRILL_RIGHT));
        }
        if (checkBottomBlock(boardPointWithActionLeft)) {
            boardPoints.add(new BoardPointWithAction(boardPointWithActionLeft.getBoardPoint().shiftBottom(), LoderunnerAction.DRILL_LEFT));
        }*/
    }

    public LoderunnerAction findAction() {
        while (!boardPoints.isEmpty()) {
            BoardPointWithAction boardPoint = boardPoints.remove();
            visitedPoints.add(boardPoint.getBoardPoint());
            BoardElement boardElement = currentGameBoard.getElementAt(boardPoint.getBoardPoint());
            switch (boardElement) {
                case NONE:
                    if (canGoLeft(boardPoint) && !visitedPoints.contains(boardPoint.getBoardPoint().shiftLeft())) {
                        boardPoints.offer(boardPoint.getLeft());
                        /*if (checkBottomBlock(boardPoint.getLeft())) {
                            boardPoints.offer(boardPoint.getLeft().getDown());
                        }*/
                    }
                    if (canGoRight(boardPoint) && !visitedPoints.contains(boardPoint.getBoardPoint().shiftRight())) {
                        boardPoints.offer(boardPoint.getRight());
                        /*if (checkBottomBlock(boardPoint.getRight())) {
                            boardPoints.offer(boardPoint.getRight().getDown());
                        }*/
                    }
                    break;
                case LADDER:
                    if (canGoLeft(boardPoint) && !visitedPoints.contains(boardPoint.getBoardPoint().shiftLeft())) {
                        boardPoints.offer(boardPoint.getLeft());
                        if (checkBottomBlock(boardPoint.getLeft())) {
                            boardPoints.offer(boardPoint.getLeft().getDown());
                        }
                    }
                    if (canGoRight(boardPoint) && !visitedPoints.contains(boardPoint.getBoardPoint().shiftRight())) {
                        boardPoints.offer(boardPoint.getRight());
                        if (checkBottomBlock(boardPoint.getRight())) {
                            boardPoints.offer(boardPoint.getRight().getDown());
                        }
                    }
                    if (canGoUp(boardPoint) && !visitedPoints.contains(boardPoint.getBoardPoint().shiftTop())) {
                        boardPoints.offer(boardPoint.getUp());
                    }
                    if (canGoDown(boardPoint) && !visitedPoints.contains(boardPoint.getBoardPoint().shiftBottom())) {
                        boardPoints.offer(boardPoint.getDown());
                    }
                    break;
                case YELLOW_GOLD:
                case GREEN_GOLD:
                case RED_GOLD:
                case SHADOW_PILL:
                    visitedPoints.clear();
                    return boardPoint.getLoderunnerAction();
            }
        }
        return defaultAction();
    }

    private boolean checkBottomBlock(BoardPointWithAction boardPoint) {
        return currentGameBoard.getElementAt(boardPoint.getBoardPoint().shiftBottom()).equals(BoardElement.BRICK);
    }

    private LoderunnerAction defaultAction() {
        counter++;
        if (DEFAULT_loderunnerAction != LoderunnerAction.DO_NOTHING) {
            return DEFAULT_loderunnerAction;
        }
        if (canGoLeft(new BoardPointWithAction(currentBoardPoint))) {
            return LoderunnerAction.GO_LEFT;
        }
        if (canGoRight(new BoardPointWithAction(currentBoardPoint))) {
            return LoderunnerAction.GO_RIGHT;
        }
        if (canGoDown(new BoardPointWithAction(currentBoardPoint))) {
            return LoderunnerAction.GO_DOWN;
        }
        if (canGoUp(new BoardPointWithAction(currentBoardPoint))) {
            return LoderunnerAction.GO_DOWN;
        }
        return LoderunnerAction.DO_NOTHING;
    }


    private boolean canGoDown(BoardPointWithAction boardPoint) {
        BoardPoint boardPointDown = boardPoint.getBoardPoint().shiftBottom();
        return currentGameBoard.hasLadderAt(boardPointDown);
    }

    private boolean canGoUp(BoardPointWithAction boardPoint) {
        BoardPoint boardPointUp = boardPoint.getUp().getBoardPoint();
        return (currentGameBoard.hasLadderAt(boardPointUp)
                || !currentGameBoard.hasBarrierAt(boardPointUp))
                && currentGameBoard.hasLadderAt(boardPoint.getBoardPoint());
    }

    private boolean canGoRight(BoardPointWithAction boardPoint) {
        BoardPoint boardPointRight = boardPoint.getRight().getBoardPoint();
        return !currentGameBoard.hasBarrierAt(boardPointRight)
                && !currentGameBoard.getElementAt(boardPoint.getBoardPoint().shiftBottom()).equals(BoardElement.NONE)
                && !currentGameBoard.hasOtherHeroAt(boardPointRight)
                && !currentGameBoard.hasPipeAt(boardPointRight.shiftBottom());
    }

    private boolean canGoLeft(BoardPointWithAction boardPoint) {
        BoardPoint boardPointLeft = boardPoint.getBoardPoint().shiftLeft();
        return !currentGameBoard.hasBarrierAt(boardPointLeft)
                && !currentGameBoard.getElementAt(boardPoint.getBoardPoint().shiftBottom()).equals(BoardElement.NONE)
                && !currentGameBoard.hasOtherHeroAt(boardPointLeft)
                && !currentGameBoard.hasPipeAt(boardPointLeft.shiftBottom());

    }


    static class BoardPointWithAction {

        @Getter
        private final BoardPoint boardPoint;
        @Getter
        @Setter
        private LoderunnerAction loderunnerAction;

        @Setter
        @Getter
        private boolean isVisited;

        public BoardPointWithAction(BoardPoint boardPoint) {
            this.boardPoint = boardPoint;
        }

        public BoardPointWithAction(BoardPoint boardPoint, LoderunnerAction loderunnerAction) {
            this.boardPoint = boardPoint;
            this.loderunnerAction = loderunnerAction;
        }

        public BoardPointWithAction getLeft() {
            return new BoardPointWithAction(boardPoint.shiftLeft(), this.getLoderunnerAction());
        }

        public BoardPointWithAction getRight() {
            return new BoardPointWithAction(boardPoint.shiftRight(), this.getLoderunnerAction());
        }

        public BoardPointWithAction getUp() {
            return new BoardPointWithAction(boardPoint.shiftTop(), this.getLoderunnerAction());
        }

        public BoardPointWithAction getDown() {
            return new BoardPointWithAction(boardPoint.shiftBottom(), this.getLoderunnerAction());
        }
    }
}
