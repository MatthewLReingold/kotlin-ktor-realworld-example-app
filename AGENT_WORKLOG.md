### AGENT WORKLOG
# Agents and Harness Used
Harness: Codex for Visual Studio Code

Model: GPT-6 Astra

I chose to use Codex over Claude as Claude models have become very verbose in their outputs as of late. This makes their generated code and comments more annoying to deal with, and also costs me, the user, more.

# Discussion on Use

The first thing I did was have the LLM explain the repo and its tech stack to me. I have not worked with Kotlin before and needed to mentally map the repo to my knowledge of other technology stacks to ensure I understood what I was working with.

Then I asked the LLM to guide me through a single existing feature so I could understand a full trace of the tech stack.

The use of an agent for development doesn't change much as far as my engineering thought process goes.

First I figured out where the feature would be best suited, and then determined edge cases to test for.

While working with an agent I still followed Test Driven Development with one change. Instead of the usual test->code->test loop as done with traditional TDD, the best way to utilize the agent is to write all the tests first and make sure you understand every edge case. 

Just like in typical TDD, the tests act as your verification, but having them all defined also acts as context for your agent to verify itself against.

Tests were designed both by me directly making them and directing the agent to make meaningful variations. Edgecases on the test focused on common failure areas such as handeling emptylists, pagination offsets and limits that would create an empty list, pramaters that lead to lists smaller than the given limit, and ensuring the lists of articles are sorted.

I allowed the agent to make more of the http tests in bulk on its own as they were conceptually simpler, and I could ensure coverage with an easy review of those tests.

 With the tests defined I allowed the agent to create the necessary function with limits to stay in the bounds of the tests and where the function needs to be written. It was not allowed to change the tests after they were finalized.

One thing the agents have a tendency to do is to consider redundant tests, such as checking if default values are passed which is really just a test of the programming language and not a necessary test from a developer standpoint. So test writing needs to remain a very human involved part of development to ensure tests are relevant and useful.

Because this repo was made with a lot of unfinished features beyond the scope of the project, regression testing, and contract testing were not concerns I took into account. I made my changes as isolated as possible to avoid touching more of the repo and creating scope creep. In a more realistic situation I would pay closer attention to regression testing and contract testing. Making sure that no downstream service, database, or application would have its requirements changed without at least communicating this to the proper owners prevents many of the largest headaches in an eneterprise environment.