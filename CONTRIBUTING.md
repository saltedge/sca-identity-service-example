# Contributing to Salt Edge Authenticator project
First off, thanks for taking the time to contribute!  

The following is a set of guidelines for contributing to projects of Salt Edge Inc, which are hosted on GitHub. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

## Code of Conduct

This project and everyone participating in it is governed by the [Salt Edge Code of Conduct](./CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. 
Please report unacceptable behavior to [Salt Edge Support](https://www.saltedge.com/pages/contact_support).

## Here are some ways *you* can contribute:

* by using alpha, beta, and prerelease versions
* by reporting bugs
* by suggesting new features
* by writing or editing documentation
* by writing specifications
* by writing code ( **no patch is too small** : fix typos, add comments, clean up inconsistent whitespace )
* by refactoring code
* by reviewing patches

### Bug Reports
In short, since you are most likely a developer, provide a ticket that you yourself would like to receive.

We depend on you (the community) to contribute in making the project better for everyone. So debug and reduce your own issues before creating a ticket and let us know of all the things you tried and their outcome. This applies double if you cannot share a reproduction with us because of internal company policies.

> **Note:** If you find a **Closed** issue that seems like it is the same thing that you're experiencing, open a new issue and include a link to the original issue in the body of your new one.

Please check if you are using the latest product version before filing a ticket.

#### How Do I Submit A (Good) Bug Report?

Bugs are tracked as [GitHub issues](https://guides.github.com/features/issues/).  

Explain the problem and include additional details to help maintainers reproduce the problem:

* **Use a clear and descriptive title** for the issue to identify the problem.
* **Describe the exact steps which reproduce the problem** in as many details as possible. For example, start by explaining how you started the app, e.g. which command exactly you used in the terminal.
* **Provide specific examples to demonstrate the steps**. Include links to files or GitHub projects, or copy/pasteable snippets, which you use in those examples. If you're providing snippets in the issue, use [Markdown code blocks](https://help.github.com/articles/markdown-basics/#multiple-lines).
* **Describe the behavior you observed after following the steps** and point out what exactly is the problem with that behavior.
* **Explain which behavior you expected to see instead and why.**
* **Include screenshots** which show you following the described steps and clearly demonstrate the problem.
* **If the problem wasn't triggered by a specific action**, describe what you were doing before the problem happened and share more information using the guidelines below.
* Include details about your configuration and environment, including your gem version, Ruby version, and operating system.
* Include a [Gist][gist] that includes a stack trace and any details that may be necessary to reproduce the bug.

Ideally, a bug report should include a pull request with failing specs.

### Pull Requests
1. [Fork the repository.][fork]
2. [Create a topic branch.][branch]
3. Follow the [style guides](#style-guides)
4. Implement your feature or bug fix.
5. Add, commit, and push your changes.
6. [Submit a pull request.][pr]

## Style Guides

### Git Commit Messages

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Pass value to..." not "Passes value to...")
* Limit the first line to 72 characters or less
* Reference issues and pull requests liberally after the first line
* When only changing documentation, include `[ci skip]` in the commit title

### Ruby style guide

Follow [Rubocop](https://github.com/rubocop-hq/ruby-style-guide) style guide

### Specs Styleguide

Follow [Betterspecs](http://www.betterspecs.org/) style guide

[gist]: https://gist.github.com/
[fork]: http://help.github.com/fork-a-repo/
[branch]: http://learn.github.com/p/branching.html
[pr]: http://help.github.com/send-pull-requests/
