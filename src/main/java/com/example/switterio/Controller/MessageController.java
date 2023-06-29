package com.example.switterio.Controller;

import com.example.switterio.domain.Message;
import com.example.switterio.domain.User;
import com.example.switterio.repository.MessageRepository;
import com.example.switterio.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.Set;


@Controller
public class MessageController {

    private final MessageService messageService;
    private final MessageRepository messageRepository;


    public MessageController(MessageService messageService,
                             MessageRepository messageRepository) {
        this.messageService = messageService;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/")
    public String greeting(Map<String, Object> model) {
        return "greeting";
    }

    @GetMapping("/main")
    public String main(
            @RequestParam(required = false, defaultValue = "")
            String filter,
            Model model,
            @PageableDefault(sort ={ "id" }, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Message> page;

        if (filter != null && !filter.isEmpty()) {
            page = messageRepository.findByTag(filter, pageable);
        } else {
            page = messageRepository.findAll(pageable);
        }

        model.addAttribute("page", page);
        model.addAttribute("url", "/main");
        model.addAttribute("filter", filter);

        return "main";
    }

    @PostMapping("/main")
    public String add(
            @AuthenticationPrincipal User user,
            @Valid Message message,
            BindingResult bindingResult,
            Model model,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        message.setAuthor(user);

        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = ControllerUtil.getErrors(bindingResult);
            model.mergeAttributes(errorMap);
            model.addAttribute("message", message);
        } else {
            messageService.saveFile(message, file);
            model.addAttribute("message", null);
        }

        Iterable<Message> messages = messageRepository.findAll();
        model.addAttribute("messages", messages);
        return "main";
    }
    @GetMapping("/user-messages/{user}")
    public String userMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable User user,
            Model model,
            @RequestParam(required = false) Message message
    ) {
        Set<Message> messages = user.getMessages();

        model.addAttribute("userChannel",user);
        model.addAttribute("subscriptionsCount", user.getSubscriptions().size());
        model.addAttribute("subscribersCount", user.getSubscribers().size());
        model.addAttribute("isSubscriber", user.getSubscribers().contains(currentUser));
        model.addAttribute("messages", messages);
        model.addAttribute("message", message);
        model.addAttribute("isCurrentUser", currentUser.equals(user));

        return "userMessages";
    }
    @PostMapping("/user-messages/{user}")
    public String updateMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long user,
            @RequestParam("id") Message message,
            @RequestParam("text") String text,
            @RequestParam("tag") String tag,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (message.getAuthor().equals(currentUser)) {
            if(StringUtils.hasLength(text)) {
                message.setText(text);
            }

            if (StringUtils.hasLength(tag)) {
                message.setTag(tag);
            }

            messageService.saveFile(message, file);

            messageRepository.save(message);
        }

        return "redirect:/user-messages/" + user;
    }
    @GetMapping("/del-user-messages/{user}")
    public String deleteMessage(
            @PathVariable Long user,
            @RequestParam("message") Long messageId
    )throws IOException{

        messageRepository.deleteById(messageId);

        return "redirect:/user-messages/" + user;
    }


}

