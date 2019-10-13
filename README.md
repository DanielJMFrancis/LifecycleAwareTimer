[![](https://jitpack.io/v/DanielJMFrancis/LifecycleAwareTimer.svg)](https://jitpack.io/#DanielJMFrancis/LifecycleAwareTimer)
# LifecycleAwareTimer

A lifecycle-aware, persistant CountDownTimer with callbacks to the current saved time.
Hooks into SharedPreferences and supports creation of multiple timers by preference key.

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
    implementation 'com.github.DanielJMFrancis:LifecycleAwareTimer:LATEST_VERSION' // -27a8f3829f-1
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

Set the timers initial time using the setter methods, e.g. `LifecycleAwareTimer.setDays(1, YOUR_TIMER_PREFERENCE_KEY)`

**Note** - There is a third parameter, a `forceSet` boolean flag, indicating whether the time should be set if the preference value already exists (true if it should be set regardless, false if it should only be set if it doesn't already exist).

#### Observing the timer

The timer exposes `LiveData` fields that emit the current values of the timer as it ticks, in addition to when it run .

```kotlin
class ExampleActivity : AppCompatActivity() {
    private val timer: LifecycleAwareTimer by lazy {
        LifecycleAwareTimer.getInstance(this.lifecycle)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        
        timer.seconds.observe(this, *Observer* { secondsInCurrentMinute ->
            secondsTextView.text = secondsInCurrentMinute
        })
        
        timer.hasTimerRunOut.observe(this, *Observer* { 
            Toast.makeText(this, "Time is up!", Toast.LENGTH_SHORT).show()
        })
    }
}
```

**Note** - The timer emits the current second in the minute it's on, the current minute in the hour it's on, the current hour in the day it's on, and the day number it's on. You'll need to format these if you'd like to display them a specific way, e.g. with zero padding.

Lastly, there is a function for returning a boolean indicating if the timer is out of time: 
`LifecycleAwareTimer.isTimerOutOfTime()` - this function takes the String identifier of the timer in question.

#### Editing a timer's current time

For active (running) timers, use the timer instance's `setActiveDays()`, `setActiveHours()`, `setActiveMinutes()`, and `setActiveSeconds()` functions. 

Otherwise, for timers that are not running, use the `LifecycleAwareTimer` static functions, passing in the value to set, an identifier string for the timer, setting the `forceSet` boolean based on whether you want to set the value if it has already been set in the user's SharedPreferences.

```kotlin
// This will set the number of days for the TIME_IDENTIFIER_STRING timer, regardless of if they've already been set
LifecycleAwareTimer.setDays(1, TIMER_IDENTIFIER_STRING, true)
```

## Contributing

Contributions and suggestions are absolutely welcome! Feel free to reach out if you'd like to collaborate or submit a PR.

## Versioning

Using [SemVer](http://semver.org/) for versioning. 

## Authors

* **Daniel Francis** - *Initial work* - [Daniel Francis](https://github.com/DanielJMFrancis)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
