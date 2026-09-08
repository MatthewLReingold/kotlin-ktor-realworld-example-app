### AGENT WORKLOG
# Agents and Harness Used
Harness: Codex for Visual Studio Code

Model: GPT-6 Astra

# Discussion on Use

The first thing I did was have the LLM explain the repo and its tech stack to me. I have not worked with Kotlin before and needed to mentally map the repo to my knowledge of other technology stacks to ensure I understood what I was working with.

Then I asked the LLM to guide me through a single existing feature so I could understand a full trace of the tech stack.

The use of an agent for development doesn't change much as far as my engineering thought process goes.

First I figured out where the feature would be best suited, and then determined edge cases to test for.

While working with an agent I still followed Test Driven Development with one change. Instead of the usual test->code->test loop as done with traditional TDD, the best way to utilize the agent is to write all the tests first and make sure you understand every edge case. 

Just like in typical TDD, the tests act as your verification, but having them all defined also acts as context for your agent to verify itself against. Once I was sure my tests had sufficient coverage of the problem, I allowed the agent to create the necessary function without further instruction, except to stay in the bounds of the tests and where the function needs to be written. Of course it was not allowed to change the tests after they were finalized.

One thing the agents have a tendency to do is to consider redundant tests, such as checking if default values are passed which is really just a test of the programming language and not a necessary test from a developer standpoint. So test writing needs to remain a very human involved part of development to ensure tests are relevant and useful.

I chose to use Codex over Claude as Claude models have become very verbose in their outputs as of late. This makes their generated code and comments more annoying to deal with, and also costs me, the user, more.