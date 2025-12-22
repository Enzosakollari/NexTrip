package com.example.demo.Controller;

import com.example.demo.Service.TravelChatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ChatController {

    private final TravelChatService travelChatService;

    public ChatController(TravelChatService travelChatService) {
        this.travelChatService = travelChatService;
    }

    @PostMapping("/chat")
    public String sendMessage(@RequestParam("message") String message, Model model) {
        String reply = travelChatService.askAssistant(message);
        model.addAttribute("userMessage", message);
        model.addAttribute("assistantReply", reply);
        return "chat";
    }

    @PostMapping("/chat/message")
    @ResponseBody
    public String sendMessageApi(@RequestBody String message) {
        return travelChatService.askAssistant(message);
    }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }
}
