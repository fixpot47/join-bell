# JoinBell

**JoinBell** is a client-side Fabric mod for Minecraft 26.3.

## What it does

- Press **J** to open the JoinBell menu.
- Add Minecraft usernames to your watch list.
- When a tracked player newly appears in the player information sent to your client, JoinBell rings Minecraft's bell once per second for about 5 seconds.
- It also listens for common server join messages as a fallback.
- The tracked-player list is stored through **FixLib's ConfigManager**, so FixLib is a real required dependency.

## Large-server limitation

JoinBell can only detect information that the server sends to your client. On a network with tens of thousands of players, a player who is on another backend/lobby and is not present in your client-side player list (and is not announced by a join message) cannot be detected by a purely client-side mod.

## Building

The project is configured for:

- Minecraft 26.3
- Fabric Loader 0.19.5
- Fabric API 0.160.5+26.3
- Java 25
- FixLib 2.2.0+26.3

The Gradle build uses Modrinth Maven for FixLib. If FixLib's Maven artifact is not public yet, put the FixLib JAR in `libs/` and replace the FixLib dependency line as described in `build.gradle`.

## Support the project

[Buy us a Cookie](https://www.donationalerts.com/r/fixpot47)

## Modrinth note

Draw the project icon manually before submission. Do not use an AI-generated icon.
