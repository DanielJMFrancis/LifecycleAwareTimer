[![](https://jitpack.io/v/DanielJMFrancis/LifecycleAwareTimer.svg)](https://jitpack.io/#DanielJMFrancis/LifecycleAwareTimer)
# LifecycleAwareTimer

A lightweight, lifecycle-aware countdown-type timer with callbacks to the remaining time left on the timer and when it finishes.

Hooks into SharedPreferences and supports creation of multiple distinct timers (by string identifier).

#### Example Use Cases
- Create a timer for a user actively using a new feature (or feature set), reward or prompt them for feedback when the timer is up
- Limit a free-version of a premium feature after a user has actively used the feature for X amount of time

## Getting Started

In your root `build.gradle` file:

```groovy
allprojects {
    repositories {
    	...
        maven { url 'https://jitpack.io' }
    }
}
```

In your dependencies (app-level `build.gradle`):

```groovy
dependencies {
    implementation 'com.github.danieljmfrancis:lifecycleawaretimer:LATEST_VERSION'
}
```

Initiate the library in your application class (or wherever you'd like) using your application's context.

```kotlin
class TimerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initiate library
        LifecycleAwareTimer.init(this)
    }
}
```

#### Instantiating the timer

Obtain an instance of a LifecycleAwareTimer using the `LifecycleAwareTimer.getInstance()` function, passing in the lifecycle the timer is attached to, and optionally a String to identify the timer (and its corresponding stored time preferences).

Set a timer's initial time using the `setTimeLeft` function.

**Note** - the `forceSet` boolean flag indicates whether the time should be set if the preference value already exists (true if it should be set regardless, false if it should only be set if it doesn't already exist).

#### Observing the timer

The timer exposes `LiveData` fields, `milliseconds` for observing the number of milliseconds left, and `onFinish` for observing when the timer finishes. 

```kotlin
class ExampleActivity : AppCompatActivity() {
    private val timer: LifecycleAwareTimer by lazy {
        LifecycleAwareTimer.getInstance(this.lifecycle)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        
        timer.milliseconds.observe(this, Observer { millisecondsLeft ->
            // Convert and display hours left
            hoursTextView.text = (millisecondsLeft / 1000 / 60 / 60).toString()
        })
        
        timer.hasTimerRunOut.observe(this, Observer { 
            Toast.makeText(this, "Time is up!", Toast.LENGTH_SHORT).show()
        })
    }
}
```

There are also functions to get the current status of a timer:
- `LifecycleAwareTimer.getTimeLeft(TimeUnit.MINUTE, "my_timer_key")`
- `LifecycleAwareTimer.isTimerOutOfTime("my_timer_key")`

#### Editing a timer's current time

For active (running) timers, use a timer's `setTimeLeftOnActiveTimer()` function.

```kotlin
val myTimer = LifecycleAwareTimer.getInstance(myActivity.lifecycle, "my_timer_key")
myTimer.setTimeLeftOnActiveTimer(seconds = 30)
```

Otherwise, use the `LifecycleAwareTimer.setTimeLeft()` function.

```kotlin
// This will set the number of seconds for the "my_timer_key" timer
// regardless of if it has already been set
LifecycleAwareTimer.setTimeLeft("my_timer_key", seconds = 30, forceSet = true)
```

## Contributing

Contributions and suggestions are absolutely welcome! Feel free to reach out if you'd like to collaborate or submit a PR.

## Versioning

Using [SemVer](http://semver.org/) for versioning. 

## Authors

* **Daniel Francis** - *Initial work* - [Daniel Francis](https://github.com/DanielJMFrancis)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
