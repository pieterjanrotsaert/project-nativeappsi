package be.pjrotsaert.mijnhogent.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.api.Chamilo
import be.pjrotsaert.mijnhogent.api.CourseData
import be.pjrotsaert.mijnhogent.viewmodels.CourseInfoViewModel
import kotlinx.android.synthetic.main.course_info_fragment.*
import kotlinx.serialization.json.JSON

class CourseInfoFragment : Fragment() {

    companion object {
        fun newInstance(courseData: String): CourseInfoFragment {
            val frag =  CourseInfoFragment()
            val bundle = Bundle()
            bundle.putString("courseData", courseData)
            frag.arguments = bundle
            return frag
        }
    }

    private lateinit var viewModel: CourseInfoViewModel
    private var courseData = CourseData()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        courseData = JSON.parse(CourseData.serializer(), arguments?.getString("courseData") ?: "{}")
        return inflater.inflate(R.layout.course_info_fragment, container, false)
    }

    override fun onDestroyView() {
        //Chamilo.getInstance(context!!).abortRequests()
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CourseInfoViewModel::class.java)
        if(viewModel.data.fullDescription.isNotEmpty())
            courseData.fullDescription = viewModel.data.fullDescription

        viewModel.data = courseData
        courseInfoRefreshPull.setOnRefreshListener { requestDescription() }

        if(viewModel.data.fullDescription.isEmpty())
            requestDescription()
        updateView()
    }

    fun updateView(){
        txtCourseCode.text = viewModel.data.chamilo_course_code
        txtCourseEdu.text = viewModel.data.training_name
        txtCourseCredits.text = viewModel.data.credits.toString()
        txtCourseType.text = viewModel.data.course_type
        txtCourseTitular.text = viewModel.data.titular_name
        txtCourseDescription.text = if(viewModel.data.fullDescription.isEmpty()) getString(R.string.course_nodescription) else viewModel.data.fullDescription
    }

    fun requestDescription(){
        courseInfoRefreshPull.isRefreshing = true
        Chamilo.getInstance(context!!).getFullCourseDescription(viewModel.data) {
            err ->
            if(view == null)
                return@getFullCourseDescription

            updateView()
            courseInfoRefreshPull.isRefreshing = false
        }
    }

}
