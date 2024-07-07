package com.ebubekir.goldgame.controllers;


import com.ebubekir.goldgame.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("board", gameService.getBoard());
        model.addAttribute("playerA", gameService.getPlayerA());
        model.addAttribute("playerB", gameService.getPlayerB());
        boolean gameOver = gameService.gameOver();
        model.addAttribute("gameOver", gameOver);
        if (gameOver) {
            model.addAttribute("summary", gameService.getGameSummary());
        }
        return "index";
    }

    @PostMapping("/move")
    public String makeMove(@RequestParam char playerType, Model model) {
        gameService.makeMove(playerType);
        return "redirect:/";
    }

    @PostMapping("/restart")
    public String restartGame() {
        gameService.initializeGame();
        return "redirect:/";
    }
}

