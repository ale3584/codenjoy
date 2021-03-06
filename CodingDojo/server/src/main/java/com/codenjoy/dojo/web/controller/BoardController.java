package com.codenjoy.dojo.web.controller;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2016 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.services.*;
import com.codenjoy.dojo.services.chat.ChatService;
import com.codenjoy.dojo.services.dao.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Controller
public class BoardController {
    public static final ArrayList<Object> EMPTY_LIST = new ArrayList<Object>();

    @Autowired private PlayerService playerService;
    @Autowired private Registration registration;
    @Autowired private ChatService chatService;
    @Autowired private GameService gameService;

    @Value("${donate.code}")
    private String donateCode;

    public BoardController() {
    }

    //for unit test
    BoardController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @RequestMapping(value = "/board/{playerName:.+}", method = RequestMethod.GET)
    public String board(ModelMap model, @PathVariable("playerName") String playerName) {
        return board(model, playerName, null);
    }

    @RequestMapping(value = "/board/{playerName:.+}", params = "code", method = RequestMethod.GET)
    public String board(ModelMap model, @PathVariable("playerName") String playerName, @RequestParam("code") String code) {
        Player player = playerService.get(playerName);
        if (player == NullPlayer.INSTANCE) {
            return "redirect:/register?name=" + playerName;
        } else {
            model.addAttribute("players", Arrays.asList(player));
            model.addAttribute("playerName", player.getName());
        }
        model.addAttribute("allPlayersScreen", false);

        setIsRegistered(model, player.getName(), code);

        GameType gameType = player.getGameType();
        model.addAttribute("boardSize", gameType.getBoardSize().getValue());
        model.addAttribute("singleBoardGame", gameType.isSingleBoard());
        model.addAttribute(AdminController.GAME_NAME, player.getGameName());
        return getBoard(model, player.getGameName());
    }

    private void setIsRegistered(ModelMap model, String expectedName, String code) {
        String actualName = registration.getEmail(code);
        boolean value = actualName != null && actualName.equals(expectedName);
        model.addAttribute("registered", value);
        model.addAttribute("code", code);
    }

    @RequestMapping(value = "/board", method = RequestMethod.GET)
    public String boardAll() {
        GameType gameType = playerService.getAnyGameWithPlayers();
        if (gameType == NullGameType.INSTANCE) {
            return "redirect:/register";
        }
        return "redirect:/board?" + AdminController.GAME_NAME + "=" + gameType.name();
    }

    @RequestMapping(value = "/board", params = AdminController.GAME_NAME, method = RequestMethod.GET)
    public String boardAllGames(ModelMap model,  @RequestParam(AdminController.GAME_NAME) String gameName) {
        if (gameName == null) {
            return "redirect:/board";
        }

        Player player = playerService.getRandom(gameName);
        if (player == NullPlayer.INSTANCE) {
            return "redirect:/register?" + AdminController.GAME_NAME + "=" + gameName;
        }
        GameType gameType = player.getGameType();
        if (gameType.isSingleBoard()) {
            return "redirect:/board/" + player.getName();
        }

        model.addAttribute("players", playerService.getAll(gameName));
        model.addAttribute("playerName", null);
        model.addAttribute(AdminController.GAME_NAME, gameName);
        setIsRegistered(model, null, null);

        model.addAttribute("boardSize", gameType.getBoardSize().getValue());
        model.addAttribute("singleBoardGame", gameType.isSingleBoard());
        model.addAttribute("allPlayersScreen", true); // TODO так клиенту припрутся все доски и даже не из его игры, надо фиксить dojo transport

        return getBoard(model, gameName);
    }

    @RequestMapping(value = "/board", params = "code", method = RequestMethod.GET)
    public String boardAll(ModelMap model, @RequestParam("code") String code) {
        String name = registration.getEmail(code);
        Player player = playerService.get(name);
        if (player == NullPlayer.INSTANCE) {
            player = playerService.getRandom(null);
        }
        if (player == NullPlayer.INSTANCE) {
            return "redirect:/register";
        }

        if (player.getGameType().isSingleBoard()) {
            return "redirect:/board/" + player.getName() + ((code != null)?"?code=" + code:"");
        }

        setIsRegistered(model, player.getName(), code);

        GameType gameType = player.getGameType();
        model.addAttribute("boardSize", gameType.getBoardSize().getValue());
        model.addAttribute("singleBoardGame", gameType.isSingleBoard());
        model.addAttribute(AdminController.GAME_NAME, player.getGameName());

        model.addAttribute("players", playerService.getAll(player.getGameName()));
        model.addAttribute("playerName", player.getName());
        model.addAttribute("allPlayersScreen", true);
        return getBoard(model, player.getGameName());
    }

    private String getBoard(ModelMap model, String gameName) {
        model.addAttribute("sprites", gameService.getSprites().get(gameName));
        model.addAttribute("sprites_alphabet", GuiPlotColorDecoder.GUI.toCharArray());
        return "board";
    }

    @RequestMapping(value = "/donate", method = RequestMethod.GET)
    public String donate(ModelMap model) {
        model.addAttribute("today", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        model.addAttribute("donateCode", donateCode);
        return "donate-form";
    }

    @RequestMapping(value = "/help")
    public String help() {
        return "help";
    }

    @RequestMapping(value = "/chat", method = RequestMethod.GET)
    public String chat(@RequestParam("playerName") String name,
                       @RequestParam("code") String code,
                       @RequestParam("message") String message)
    {
        Player player = playerService.get(registration.getEmail(code));
        if (player != NullPlayer.INSTANCE && player.getName().equals(name)) {
            chatService.chat(player.getName(), message);
        }
        return "ok";
    }
}
