# 🃏 PvP Card Game — README

## 🚀 How to Run the Project

1. Clone this repository:

   ```bash
   git clone https://github.com/your-username/your-repository.git
   ```
2. Open the project in your preferred IDE (we used Intellij)
3. To verify that the Java module is in the correct path: Go to File → Project Structure  (Ctrl + Alt + Shift + S).
In the left menu, go to Modules
Click on + → New Module
Choose Java
In the folder field, select the project directory (InscryptionPvP).
Click Finish.
4. In IntelliJ:  
Right-click on the "src" folder →
Select Mark Directory as → Sources Root.   
Right-click on the "resources" folder → Select Mark Directory as → Resources Root. 
5. We used JavaFX to run UI, so you have to 
add "lib" folder of JavaFX:  Go to File → Project Structure → Libraries → "+" button → select Java → Search for local where you installed JavaFX (i.e: D:\JavaFX\javafx-sdk-25.0.1\lib)
6. run -> edit configurations -> add new configuration -> application -> modify options -> add VM options   
add: --module-path "PATH_HERE" --add-modules javafx.controls,javafx.fxml   
The path is the JavaFX lib folder.
7. Compile and run the main game class

---


## 📌 Overview

This project is a **PvP (Player vs Player) card game** inspired by classic turn-based strategy games, with strong emphasis on sacrifice mechanics, resource management, and tactical board positioning.

The game was developed using **Object-Oriented Programming (OOP)** and **Event-Oriented Programming (EOP)** concepts, ensuring a modular, organized, and easily maintainable architecture.

The main goal is to deliver an intense and strategic competitive experience between two players.

---

## 🎮 Game Features

* **Local PvP** gameplay (player versus player)
* **4x4 board**, optimized for strategic matches
* Card system with **multiple cost types** (blood, bones, and zero cost)
* Core mechanics including **sacrifice**, **items**, **sigils**, and **damage scale**
* **Dark thematic** and atmospheric style
* Turn-based gameplay

---

## 🧠 Core Mechanics

### 🃴 Cards

The game features different card types:

* **No-cost cards**: mainly used as resources (e.g., squirrels)
* **Blood-cost cards**: require sacrifices to be played
* **Bone-cost cards**: require a specific amount of accumulated bones

### 🩸 Sacrifice System

To play blood-cost cards, players must sacrifice other cards. Most cards grant **1 sacrifice point**, except those with special sigils that increase this value.

### 🦴 Bones

Whenever a card is destroyed or sacrificed, the player gains **bones**, which act as an alternative resource for summoning certain cards.

### ✨ Sigils

Sigils are special abilities attached to cards, enabling unique actions such as:

* Multi-directional attacks
* Special movement
* Flight
* Unique board interactions

### 🎒 Items

Items are powerful tools capable of drastically changing the outcome of a match. They can be obtained in three ways:

* Random starting item
* Through cards with specific sigils
* When a player reaches **1 remaining life**

### ⚔️ Combat and Board

* Cards are first placed on a **placement tile**
* After one round, they automatically move to the **attack tile**
* If no opposing card is present, damage is dealt directly to the opponent

### ⚖️ Damage Scale System

Each player has a damage scale:

* When it reaches **5 weights against the player**, they lose **1 life**
* Each player starts with **2 lives**

---

## 🏗️ Project Architecture

### 📦 Object-Oriented Programming (OOP)

OOP is used to model the main game entities, such as:

* **Cards** (base classes and specializations)
* **Players**
* **Decks**
* **Board**
* **Items and Sigils**

Applied principles include:

* Encapsulation
* Inheritance
* Polymorphism
* Single Responsibility Principle

### ⚡ Event-Oriented Programming (EOP)

Game logic is heavily driven by events, including:

* Playing a card
* Sacrificing a card
* Turn start and end
* Sigil activation
* Item usage
* Combat resolution

This approach allows new mechanics to be added with minimal impact on existing code.

---

## 🛠️ Technologies Used

* Language: **Java** (adjust if necessary)
* Paradigms: **OOP and EOP**
* Graphical Interface: **JavaFX** (if applicable)

---

## 👥 Authors

* Rafael Martiniano Nogueira Filho
* Tiago Alves Rodrigues
* Artur Yano Contarelli

This project was developed as an academic assignment, focusing on software engineering best practices, clean architecture, and game design principles.

---

## 📄 License

This project is intended for **educational use**. See the license file for more information.

---

🎯 *This game was created to combine software engineering concepts and game design into a practical and challenging experience.*

