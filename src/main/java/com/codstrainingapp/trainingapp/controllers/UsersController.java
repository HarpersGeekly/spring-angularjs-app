package com.codstrainingapp.trainingapp.controllers;

import com.codstrainingapp.trainingapp.models.Password;
import com.codstrainingapp.trainingapp.models.Post;
import com.codstrainingapp.trainingapp.models.PostVote;
import com.codstrainingapp.trainingapp.models.User;
import com.codstrainingapp.trainingapp.services.UserService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class UsersController {

    private UserService userSvc;

    @Autowired
    public UsersController(UserService userSvc) {
        this.userSvc = userSvc;
    }

//------------------------------------------- Login -------------------------------------------------

    @GetMapping("/login")
    public String showLoginForm(Model viewModel, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            return "redirect:/profile";
        }
        viewModel.addAttribute("message", "Welcome to the login page - TEST");
        viewModel.addAttribute("user", new User());
        return "users/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("user") User user, Model viewModel, HttpServletRequest request, RedirectAttributes redirect) {
        boolean usernameIsEmpty = user.getUsername().isEmpty();
        boolean passwordIsEmpty = user.getPassword().isEmpty();
        User existingUser = userSvc.findByUsername(user.getUsername());
        boolean userExists = (existingUser != null);

        boolean validAttempt = userExists && existingUser.getUsername().equals(user.getUsername()) && Password.check(user.getPassword(), existingUser.getPassword());

        if (userExists && !passwordIsEmpty) {
            boolean passwordCorrect =  Password.check(user.getPassword(), existingUser.getPassword()); // check the submitted password against what I have in the database
            // incorrect password:
            if (!passwordCorrect) {
                viewModel.addAttribute("passwordIsIncorrect", true);
                viewModel.addAttribute("user", user);
                return "users/login";
            }
        }

        // both empty...
        if (usernameIsEmpty && passwordIsEmpty) {
            viewModel.addAttribute("usernameIsEmpty", true);
            viewModel.addAttribute("passwordIsEmpty", true);
            return "users/login";
        }
        //one or the other...
        if (!usernameIsEmpty && passwordIsEmpty) {
            viewModel.addAttribute("passwordIsEmpty", true);
            viewModel.addAttribute("user", user);
            return "users/login";
        }
        if (usernameIsEmpty && !passwordIsEmpty) {
            viewModel.addAttribute("usernameIsEmpty", true);
            viewModel.addAttribute("user", user);
            return "users/login";
        }

        // username doesn't exist:
        if ((!usernameIsEmpty && !passwordIsEmpty) && !userExists) {
            request.setAttribute("userNotExist", !userExists);
            return "users/login";
        }

        if (!validAttempt) {
            System.out.println("not a valid attempt");
            viewModel.addAttribute("errorMessage", true);
            viewModel.addAttribute("user", user);
            return "users/login";
        } else {
            request.getSession().setAttribute("user", existingUser);
            redirect.addFlashAttribute("user", existingUser);
            return "redirect:/profile";
        }
    }

//-------------------------------------- Logout ----------------------------------------------------

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        request.getSession().invalidate();
        return "redirect:/login";
    }

//-------------------------------------- Register --------------------------------------------------

    @GetMapping("/register")
    public String register(Model viewModel, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            return "redirect:/profile";
        }
        viewModel.addAttribute("user", new User());
        return "users/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            Model viewModel,
            HttpServletRequest request,
            @RequestParam(name = "confirm_password") String passwordConfirmation) {

        if (user.getBio().isEmpty()) {
            user.setBio(null);
        } else {
            user.setBio(user.getBio());
        }

        User existingUser = userSvc.findByUsername(user.getUsername());
        if (existingUser != null) {
            result.rejectValue(
                    "username",
                    "user.username",
                    "Username is already taken.");
        }

        User existingEmail = userSvc.findByEmail(user.getEmail());
        if (existingEmail != null) {
            result.rejectValue(
                    "email",
                    "user.email",
                    "Email is already used.");
        }

        //compare passwords:
        if (!passwordConfirmation.equals(user.getPassword())) {
            result.rejectValue(
                    "password",
                    "user.password",
                    "Your passwords do not match.");
        }

        // if there are errors, show the form again.
        if (result.hasErrors()) {
            result.getFieldErrors().stream().forEach(f -> System.out.println((f.getField() + ": " + f.getDefaultMessage())));
            System.out.println("errors");
            viewModel.addAttribute("errors", result);
            viewModel.addAttribute("user", user);
            return "users/register";
        }

//        //TODO need to set to null???
//        user.setPosts(null);
        user.setPassword(Password.hash(user.getPassword()));
        user.setDate(LocalDateTime.now());
        userSvc.save(user);
        request.getSession().setAttribute("user", user);
        return "redirect:/profile";
    }

// ---------------------------- Profile -------------------------------------------------------

    @GetMapping("/profile")
    public String showProfile(HttpServletRequest request) {
        User sessionUser = (User) request.getSession().getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        User user = userSvc.findOne(sessionUser.getId());
        return "redirect:/profile/" + user.getId() + '/' + user.getUsername();
    }

    @GetMapping("/profile/{id}/{username}")
    public String showOtherUsersProfile(@PathVariable long id, Model viewModel) {
        User user = userSvc.findOne(id);
        viewModel.addAttribute("user", user);
        return "users/profile";
    }

//----------------------- Get User -------------------------------------------------------

    @GetMapping(value = "/getUser/{id}")
    @ResponseBody // this method currently returns a User as json string.
    public ObjectNode fetchUser(@PathVariable(name="id") long id) throws JsonProcessingException {
        User user = userSvc.findOne(id);
        return userSvc.toJson(user);
    }

//---------------------- Update User ---------------------------------------------------

    @PostMapping("/editUser/{id}")
    @ResponseBody
    public User updateUser(@PathVariable("id") long id, @RequestBody User user, Model viewModel) {
//        User updatedUser = userSvc.findOne(id); // Do not need to find user, user is already provided by @RequestBody User user, which is the converted JSON string back to user object.
        User updatedUser = userSvc.update(id, user.getUsername(), user.getEmail(), user.getBio());
        viewModel.addAttribute("user", updatedUser);
        return updatedUser;
    }

//--------------------- Delete User ----------------------------------------------------

    @PostMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable("id") long id, HttpServletRequest request, RedirectAttributes redirect, Model viewModel) {
        User user = userSvc.findOne(id);
        userSvc.delete(user);
        request.getSession().removeAttribute("user");
        request.getSession().invalidate();
        redirect.addFlashAttribute("successDelete", userSvc.findOne(id) == null);
        redirect.addFlashAttribute("successMessage", "Sorry to see you go! Your account has been deactivated.");
        return "redirect:/register";
    }

}



